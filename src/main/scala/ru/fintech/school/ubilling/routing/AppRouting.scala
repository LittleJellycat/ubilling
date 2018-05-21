package ru.fintech.school.ubilling.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives._
import ru.fintech.school.ubilling.domain.UserResponse._
import ru.fintech.school.ubilling.domain._
import ru.fintech.school.ubilling.handler._

object AppRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with FutureDirectives
  with FileUploadDirectives
  with CodingDirectives
  with SprayJsonSupport {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def billRoute(handler: BillRequestHandler): Route = pathPrefix("bill") {
    // /bill/upload/{bill-id}
    path("upload" / JavaUUID) { billId =>
      decodeRequest {
        (post & fileUpload("img")) { case (_, src) => // TODO: smth with meta
          complete(handler.processPhotoStream(billId, 0L, src))
        }
      }
    } ~
      // /bill/{bill-id}
      (get & path(JavaUUID)) { billId =>
        complete(handler.findBill(billId))
      } ~ // /bill/{bill-id}/users
      (get & path(JavaUUID / "users")) { billId =>
        complete(handler.findUserIds(billId))
      } ~ // /bill/
      (post & pathEnd & entity(as[BillView])) { bill =>
        complete(handler.addBill(bill))
      } ~ // /bill/photo
      (get & path(JavaUUID / "photo")) { billId =>
        complete(handler.getPhoto(billId))
      }
  }

  def userRoute(handler: UserRequestHandler): Route = pathPrefix("user") {
    // api/v1/user/assign-bill/{bill-id}&uid={user-id}
    (post & path("assign-bill" / JavaUUID)
      & parameter('uid.as[Long])) { (bid, uid) =>
      complete(handler.assignBill(bid, uid))
    } ~
      (get & pathPrefix(LongNumber)) { userId =>
        // api/v1/user/{user-id}/bills/
        path("bills") {
          complete(handler.findBillIds(userId))
        } ~ // api/v1/user/{user-id}
          pathEnd {
            complete(handler.findUser(userId))
          }
      } ~ // api/v1/user/
      (pathEnd & post & entity(as[UserView])) { user =>
        complete(handler.addUser(user))
      }
  }

  def route(handler: RequestHandler): Route = {
    pathPrefix("api" / "v1") {
      billRoute(handler) ~ userRoute(handler)
    }
  }
}

