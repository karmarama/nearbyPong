package agency.nice.nearbypong.widgets

/**
 * Created by fernando.moyano on 07/09/2017.
 */
class Endpoint(val id: String, val name: String) {

    override fun equals(obj: Any?) =
        if (obj != null && obj is Endpoint) {
            val other = obj as Endpoint?
            id == other!!.id
        } else false

    override fun hashCode() = id.hashCode()

    override fun toString() = String.format("Endpoint{id=%s, name=%s}", id, name)
}