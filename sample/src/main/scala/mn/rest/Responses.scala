package mn.rest

import mn.model.model.User
import mn.rest.Responses.ResponseError
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object Responses {

  case class ResponseError(id: Int, msg: String)

}

object SampleJsonProtocols extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val errorFormat: RootJsonFormat[ResponseError] = jsonFormat2(ResponseError)
}

