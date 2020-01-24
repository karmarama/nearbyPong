package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.R
import agency.nice.nearbypong.model.NearbyMessage
import agency.nice.nearbypong.widgets.Endpoint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.squareup.moshi.Moshi
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by fernando.moyano on 07/09/2017.
 */
abstract class ConnectionsActivity : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    val TAG = "NearbyText"
    private val SERVICE_ID = "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID"
    private lateinit var state: State
    private lateinit var name: String
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private val TIMEOUT_STATUS = 15
    private val TIMEOUT_CONNECT_MILLIS: Long = 30000
    private val TIMEOUT_MESSAGE_MILLIS: Long = 20000
    private val TIMEOUT_ADVERTISING_MILLIS: Long = 25000
    private val TIMEOUT_DISCOVERY_MILLIS: Long = 25000
    protected val SECOND_MILLIS: Long = 1000

    private val STRATEGY = Strategy.P2P_STAR
    private val discoveredEndpoints = HashMap<String, Endpoint>()
    private val pendingConnections = HashMap<String, Endpoint>()
    private val establishedConnections = HashMap<String, Endpoint>()
    private var googleApiClient: GoogleApiClient? = null
    private var isConnecting = false
    private var isDiscovering = false
    private var isAdvertising = false
    private var connectionAccepted = false
    private var onPauseReached = false


    private lateinit var countDownTimer: CountDownTimer

    /**
     * States that the UI goes through.
     */
    enum class State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }

    /**
     * Nearby Connection cases
     */
    enum class ConnectionCase {
        ACCEPT_CONNECTION,
        START_DISCOVERY,
        START_ADVERTISING,
        REQUEST_CONNECTION,
        SEND_PAYLOAD
    }

    init {
        state = State.UNKNOWN
        countDownTimer = object : CountDownTimer(TIMEOUT_DISCOVERY_MILLIS, SECOND_MILLIS) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("ADVERT", "seconds remaining: " + millisUntilFinished / SECOND_MILLIS)
            }

            override fun onFinish() {
                if (!connectionAccepted) {
                    onTimeOut()
                }
            }
        }
    }

    /**
     * Callbacks for payloads (bytes of data) sent from another device to us.
     */
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(
                TAG,
                String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload)
            );
            onReceive(establishedConnections[endpointId], payload)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(
                TAG,
                String.format(
                    "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update
                )
            )
        }
    }

    /**
     * Callbacks for connections to other devices.
     */
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(
                TAG, String.format(
                    "onConnectionInitiated(endpointId=%s, endpointName=%s)",
                    endpointId, connectionInfo.endpointName
                )
            )
            val endpoint = Endpoint(endpointId, connectionInfo.endpointName)
            pendingConnections.put(endpointId, endpoint)
            this@ConnectionsActivity.onConnectionInitiated(endpoint, connectionInfo)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(
                TAG,
                String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result)
            )

            // We're no longer connecting
            isConnecting = false

            if (!result.status.isSuccess) {
                Log.d(
                    TAG,
                    String.format(
                        "Connection failed. Received status %s.",
                        statusToString(result.status)
                    )
                )
                onConnectionFailed()
                return
            }
            connectedToEndpoint(pendingConnections.remove(endpointId)!!)
        }

        override fun onDisconnected(endpointId: String) {
            if (!establishedConnections.containsKey(endpointId)) {
                Log.d(TAG, "Unexpected disconnection from endpoint " + endpointId)
                return
            }
            disconnectedFromEndpoint(establishedConnections[endpointId])
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseReached = true
    }

    override fun onResume() {
        super.onResume()
        if (onPauseReached) {
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = getDeviceId()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (googleApiClient != null) {
            resetState()
            stopNearbyActions()
            if (countDownTimer != null) {
                countDownTimer?.cancel()
                countDownTimer?.onFinish()
            }

        }

    }

    private fun startConnectionTimer() {
        countDownTimer.start()
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun statusToString(status: Status): String {
        return String.format(
            Locale.US,
            "[%d]%s",
            status.statusCode,
            if (status.statusMessage != null)
                status.statusMessage
            else
                ConnectionsStatusCodes.getStatusCodeString(status.statusCode)
        )
    }

    /**
     * @return True if the app was granted all the permissions. False otherwise.
     */
    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    protected fun resetState() {
        disconnectFromAllEndpoints()
        pendingConnections.clear()
        isConnecting = false
        isDiscovering = false
        isAdvertising = false
        connectionAccepted = false
    }

    private fun createGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onStart() {
        if (hasPermissions(this, *getRequiredPermissions())) {
            createGoogleApiClient()
        } else {
            requestPermissions(getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
        super.onStart()
    }


    /**
     * We've connected to Nearby Connections' GoogleApiClient.
     */
    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "onConnected")
        setState(State.SEARCHING)
    }

    /**
     * We've been temporarily disconnected from Nearby Connections' GoogleApiClient.
     */
    @CallSuper
    override fun onConnectionSuspended(reason: Int) {
        Log.d(TAG, String.format("onConnectionSuspended(reason=%s)", reason))
        resetState()
        stopNearbyActions()
        onTimeOutReached()
        setState(State.UNKNOWN)
    }

    /**
     * We are unable to connect to Nearby Connections' GoogleApiClient. Oh uh.
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(
            TAG,
            String.format(
                "onConnectionFailed(%s)",
                statusToString(Status(connectionResult.errorCode))
            )
        )
    }

    /**
     * The user has accepted (or denied) our permission request.
     */
    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, getString(R.string.error_missing_permissions))
                    finish()
                    return
                }
            }
            createGoogleApiClient()
            onPauseReached = false
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Sets the device to advertising mode. It will broadcast to other devices in discovery mode.
     * Either [.onAdvertisingStarted] or [.onAdvertisingFailed] will be called once
     * we've found out if we successfully entered this mode.
     */
    protected fun startAdvertising() {
        isAdvertising = true
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.startAdvertising(
                googleApiClient,
                getName(),
                getServiceId(),
                connectionLifecycleCallback,
                AdvertisingOptions(STRATEGY)
            )
                .setResultCallback({ result ->
                    onResult(ConnectionCase.START_ADVERTISING, result.status)
                }, TIMEOUT_ADVERTISING_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * Stops advertising.
     */
    protected fun stopAdvertising() {
        isAdvertising = false
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.stopAdvertising(googleApiClient)
        }
    }

    /**
     * Advertising has successfully started. Override this method to act on the event.
     */
    protected fun onAdvertisingStarted() {
        connectionAccepted = false
        startConnectionTimer()
    }

    /**
     * Advertising has failed to start. Override this method to act on the event.
     */
    protected fun onAdvertisingFailed() {}

    private fun send(payload: Payload, endpoints: Set<String>) {
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.sendPayload(googleApiClient, ArrayList(endpoints), payload)
                .setResultCallback(
                    { status -> onResult(ConnectionCase.SEND_PAYLOAD, status) },
                    TIMEOUT_MESSAGE_MILLIS,
                    TimeUnit.MILLISECONDS
                )

        }
    }

    /**
     * Stops discovery.
     */
    protected fun stopDiscovering() {
        isDiscovering = false
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.stopDiscovery(googleApiClient)
        }
    }

    protected fun disconnectFromAllEndpoints() {
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            for (endpoint in establishedConnections.values) {
                Nearby.Connections.disconnectFromEndpoint(googleApiClient, endpoint.id)
            }
        }
        discoveredEndpoints.clear()
        establishedConnections.clear()
    }


    /**
     * Accepts a connection request.
     */
    protected fun acceptConnection(endpoint: Endpoint) {
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.acceptConnection(googleApiClient, endpoint.id, payloadCallback)
                .setResultCallback { status ->
                    onResult(ConnectionCase.ACCEPT_CONNECTION, status)
                }
        }
    }

    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
     * [.onDiscoveryStarted] ()} or [.onDiscoveryFailed] ()} will be called once we've
     * found out if we successfully entered this mode.
     */
    protected fun startDiscovering() {
        isDiscovering = true
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.startDiscovery(
                googleApiClient,
                getServiceId(),
                object : EndpointDiscoveryCallback() {
                    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                        Log.d(
                            TAG,
                            String.format(
                                "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                                endpointId, info.serviceId, info.endpointName
                            )
                        )

                        if (getServiceId() == info.serviceId) {
                            val endpoint = Endpoint(endpointId, info.endpointName)
                            discoveredEndpoints.put(endpointId, endpoint)
                            onEndpointDiscovered(endpoint)
                        }
                    }

                    override fun onEndpointLost(endpointId: String) {
                        Log.d(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId))
                    }
                },
                DiscoveryOptions(STRATEGY)
            )
                .setResultCallback(
                    { status -> onResult(ConnectionCase.START_DISCOVERY, status) },
                    TIMEOUT_DISCOVERY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
        }
    }


    /**
     * Sends a connection request to the endpoint.
     */
    protected fun connectToEndpoint(endpoint: Endpoint) {
        // If we already sent out a connection request, wait for it to return
        // before we do anything else. P2P_STAR only allows 1 outgoing connection.
        if (isConnecting) {
            Log.d(TAG, "Already connecting, so ignoring this endpoint: " + endpoint)
            return
        }

        Log.d(TAG, "Sending a connection request to endpoint " + endpoint)
        // Mark ourselves as connecting so we don't connect multiple times
        isConnecting = true

        // Ask to connect
        if (googleApiClient!!.isConnected) {
            Nearby.Connections.requestConnection(
                googleApiClient, getName(), endpoint.id, connectionLifecycleCallback
            )
                .setResultCallback(
                    { status -> onResult(ConnectionCase.REQUEST_CONNECTION, status) },
                    TIMEOUT_CONNECT_MILLIS,
                    TimeUnit.MILLISECONDS
                )

        }
    }


    private fun onResult(connectionCase: ConnectionCase, status: Status) {
        when (connectionCase) {
            ConnectionCase.ACCEPT_CONNECTION -> {
                if (!status.isSuccess) {
                    Log.d(
                        TAG,
                        String.format(
                            "acceptConnection failed. %s", statusToString(status)
                        )
                    )
                    connectionAccepted = false
                } else {
                    connectionAccepted = true
                }
            }
            ConnectionCase.START_DISCOVERY -> {
                if (status.isSuccess) {
                    onDiscoveryStarted()
                } else {
                    if (status.statusCode == TIMEOUT_STATUS) {
                        Log.d(TAG, "TimeOut reached")
                        onTimeOut()
                    } else {
                        isDiscovering = false
                        Log.d(
                            TAG,
                            String.format(
                                "Discovering failed. Received status %s.",
                                statusToString(status)
                            )
                        )
                        onDiscoveryFailed()
                    }
                    connectionAccepted = false
                }
            }
            ConnectionCase.START_ADVERTISING -> {
                if (status.isSuccess) {
                    onAdvertisingStarted()
                } else {
                    if (status.statusCode == TIMEOUT_STATUS) {
                        Log.d(TAG, "TimeOut reached")
                        onTimeOut()
                    } else {
                        Log.d(
                            TAG,
                            String.format(
                                "Advertising failed. Received status %s.",
                                statusToString(status)
                            )
                        )
                        onAdvertisingFailed()
                    }
                    isAdvertising = false
                    connectionAccepted = false

                }
            }
            ConnectionCase.REQUEST_CONNECTION -> {
                if (!status.isSuccess) {
                    if (status.statusCode == TIMEOUT_STATUS) {
                        Log.d(TAG, "TimeOut reached")
                        onTimeOut()
                    } else {
                        Log.d(
                            TAG,
                            String.format(
                                "requestConnection failed. %s", statusToString(status)
                            )
                        )
                        onConnectionFailed()
                    }
                    isConnecting = false
                    connectionAccepted = false
                }

            }
            ConnectionCase.SEND_PAYLOAD -> {
                if (!status.isSuccess) {
                    if (status.statusCode == TIMEOUT_STATUS) {
                        Log.d(TAG, "TimeOut reached")
                        onTimeOut()
                    } else {
                        Log.d(
                            TAG, (String.format(
                                "sendUnreliablePayload failed. %s",
                                statusToString(status)
                            ))
                        )
                    }
                }
            }
        }
    }

    /**
     * Sends a [Payload] to all currently connected endpoints.
     *
     * @param payload The data you want to send.
     */
    protected fun send(payload: Payload) {
        send(payload, establishedConnections.keys)
    }


    /**
     * Discovery has successfully started. Override this method to act on the event.
     */
    protected fun onDiscoveryStarted() {}

    /**
     * Discovery has failed to start. Override this method to act on the event.
     */
    protected fun onDiscoveryFailed() {}


    private fun connectedToEndpoint(endpoint: Endpoint) {
        Log.d(TAG, String.format("connectedToEndpoint(endpoint=%s)", endpoint))
        establishedConnections.put(endpoint.id, endpoint)
        onEndpointConnected(endpoint)
    }

    private fun disconnectedFromEndpoint(endpoint: Endpoint?) {
        Log.d(TAG, String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint))
        establishedConnections.remove(endpoint!!.id)
        onEndpointDisconnected(endpoint)
    }

    /**
     * @return A list of currently connected endpoints.
     */
    private fun getDiscoveredEndpoints(): Set<Endpoint> {
        val endpoints = HashSet<Endpoint>()
        endpoints.addAll(discoveredEndpoints.values)
        return endpoints
    }

    /**
     * @return A list of currently connected endpoints.
     */
    protected fun getConnectedEndpoints(): Set<Endpoint> {
        val endpoints = HashSet<Endpoint>()
        endpoints.addAll(establishedConnections.values)
        return endpoints
    }


    /**
     * An optional hook to pool any permissions the app needs with the permissions ConnectionsActivity
     * will request.
     *
     * @return All permissions required for the app to properly function.
     */
    protected open fun getRequiredPermissions(): Array<String> {
        return REQUIRED_PERMISSIONS
    }

    override fun onStop() {
        // After our Activity stops, we disconnect from Nearby Connections.
        setState(State.UNKNOWN)
        super.onStop()
    }

    override fun onBackPressed() {
        if (getState() == State.CONNECTED) {
            setState(State.SEARCHING)
            return
        }
        super.onBackPressed()
    }


    fun onEndpointDiscovered(endpoint: Endpoint) {
        // We found an advertiser!
        connectToEndpoint(endpoint)
    }


    fun onConnectionInitiated(endpoint: Endpoint, connectionInfo: ConnectionInfo) {
        // We accept the connection immediately.
        acceptConnection(endpoint)
    }

    abstract fun onEndpointConnected(endpoint: Endpoint?)

    abstract fun onEndpointDisconnected(endpoint: Endpoint?)

    abstract fun onTimeOutReached()


    fun onConnectionFailed() {
        // Let's try someone else.
        if (getState() == State.SEARCHING && !getDiscoveredEndpoints().isEmpty()) {
            connectToEndpoint(pickRandomElem(getDiscoveredEndpoints()))
        } else {
            onTimeOut()
        }
    }

    fun onTimeOut() {
        resetState()
        stopNearbyActions()
        onTimeOutReached()
        setState(State.UNKNOWN)
    }

    private fun getState(): State {
        return state
    }

    /**
     * The state has changed. I wonder what we'll be doing now.
     *
     * @param state The new state.
     */
    protected fun setState(state: State) {
        if (this.state == state) {
            log("State set to $state but already in that state")
            return
        }

        log("State set to " + state)
        this.state = state
        onStateChanged(state)
    }

    private inline fun <reified T> pickRandomElem(collection: Collection<T>): T {
        return collection.toTypedArray()[Random().nextInt(collection.size)]
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    protected fun onStateChanged(newState: State) {

        // Update Nearby Connections to the new state.
        when (newState) {
            State.SEARCHING -> {
                startDiscovering()
                startAdvertising()
            }
            State.CONNECTED -> {
                stopNearbyActions()
            }
            else -> {
            }
        }

    }

    private fun stopNearbyActions() {
        stopDiscovering()
        stopAdvertising()
    }


    abstract fun onReceive(endpoint: Endpoint?, payload: Payload)

    private fun generateBytes(message: String): ByteArray {
        return message.toByteArray(Charset.forName("UTF-8"))
    }

    /**
     * Starts recording sound from the microphone and streaming it to all connected devices.
     */
    protected fun sendMsg(message: NearbyMessage) {
        log("sendMsg(): " + message)
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(NearbyMessage::class.java)
        val json: String = jsonAdapter.toJson(message)
        log("sendMsg() JSON: " + json)

        send(Payload.fromBytes(generateBytes(json)))
    }

    /**
     * Queries the phone's contacts for their own profile, and returns their name. Used when
     * connecting to another device.
     */
    fun getName(): String {
        return name
    }

    /**
     * {@see ConnectionsActivity#getServiceId()}
     */
    fun getServiceId(): String {
        return SERVICE_ID
    }

    protected fun log(msg: String) {
        Log.v(TAG, msg)
        appendToLogs(toColor(msg, resources.getColor(R.color.log_verbose)))
    }

    private fun toColor(msg: String, color: Int): CharSequence {
        val spannable = SpannableString(msg)
        spannable.setSpan(ForegroundColorSpan(color), 0, msg.length, 0)
        return spannable
    }


    private fun appendToLogs(msg: CharSequence) {
        Log.d(TAG, "\n" + DateFormat.format("hh:mm", System.currentTimeMillis()).toString() + ": ")
        Log.d(TAG, msg.toString())
    }

    protected fun appendResultToLogs(msg: String) {
        Log.d(TAG, "\nJust received:\n")
        Log.d(TAG, DateFormat.format("hh:mm", System.currentTimeMillis()).toString() + ": ")
        Log.d(TAG, msg)
    }

}
