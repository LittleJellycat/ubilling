package ru.fintech.school.ubilling.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{FutureDirectives, MethodDirectives, PathDirectives, RouteDirectives}
import ru.fintech.school.ubilling.domain.UserResponse._
import ru.fintech.school.ubilling.domain._
import ru.fintech.school.ubilling.handler.{BillService, RequestHandler, UserBillService, UserService}

import scala.concurrent.Future

object AppRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with SprayJsonSupport
  with FutureDirectives {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def route(
    handler: RequestHandler
  ): Route = {
    pathPrefix("api" / "v1") {
      pathPrefix("bill") {
        // api/v1/bill/{bill-id}
        (get & path(JavaUUID)) { billId =>
          complete(handler.findBill(billId))
        } ~ // api/v1/bill/{bill-id}/users
          (get & path(JavaUUID / "users")) { billId =>
            complete(handler.findUserIds(billId))
          } ~ // api/v1/bill/
          (pathEnd & post & entity(as[BillView])) { bill =>
            complete(handler.addBill(bill))
          }
      } ~
        pathPrefix("user") {
          // api/v1/user/assign-bill/{bill-id}&uid={user-id}
          (post & path("assign-bill" / JavaUUID)
            & parameter('uid.as[Long])) { (bid, uid) =>
            complete(handler.assignBill(bid, uid))
          } ~
            get {
              // api/v1/user/{user-id}/bills/
              path(LongNumber / "bills") { userId =>
                complete(handler.findBillIds(userId))
              } ~ // api/v1/user/{user-id}
                path(LongNumber) { userId =>
                  complete(handler.findUser(userId))
                }
            } ~ // api/v1/user/
            (pathEnd & post & entity(as[UserView])) { user =>
              complete(handler.addUser(user))
            }
        }
    }
  }
}

