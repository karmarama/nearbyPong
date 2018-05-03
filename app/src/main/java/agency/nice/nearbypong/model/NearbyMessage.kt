package agency.nice.nearbypong.model

/**
 * Created by fernando.moyano on 11/09/2017.
 */

data class NearbyMessage(val id: String = "", val confirmation: String = "", val end: String = "", val position: BallParameters = BallParameters(), val goal: Goal = Goal())

