package ru.fintech.school.ubilling.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{FutureDirectives, MethodDirectives, PathDirectives, RouteDirectives}
import ru.fintech.school.ubilling.domain.UserResponse._
import ru.fintech.school.ubilling.domain._
import ru.fintech.school.ubilling.handler.{BillService, UserBillService, UserService}

object AppRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with SprayJsonSupport
  with FutureDirectives {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def route(
    billService: BillService,
    userService: UserService,
    userBillService: UserBillService
  ): Route = {
    pathPrefix("api" / "v1") {
      pathPrefix("bill") {
        // api/v1/bill/{bill-id}
        (get & path(JavaUUID)) { billId =>
          onSuccess(billService.findBill(billId) zip userBillService.findUserIds(billId)) {
            case (Some(bill), users) => complete(BillResponse(billId, bill))
            case _ => complete(HttpResponse(StatusCodes.NotFound))
          } // api/v1/bill
        } ~ (post & entity(as[BillView])) { bill =>
          onSuccess(billService.addBill(bill)) {
            case Some(billId) => complete(billId.toString)
            case None => complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      } ~ pathPrefix("user") {
        post {
          // api/v1/user/assign-bill/{bill-id}&uid={user-id}
          (path("assign-bill" / JavaUUID) & parameter('uid.as[Long])) { (bid, uid) =>
            onSuccess(userBillService.assignBill(uid, bid)) { _ =>
              complete(HttpResponse(StatusCodes.OK))
            }
          }
        } ~ get {
          // api/v1/user/{user-id}/bills/
          path(LongNumber / "bills") { userId =>
            onSuccess(userBillService.findBillIds(userId)) { ids =>
              complete(ids.mkString)
            } // api/v1/user/{user-id}
          } ~ path(LongNumber) { userId =>
            onSuccess(userService.findUser(userId) zip userBillService.findBillIds(userId)) {
              case (Some(u), bills) => complete(u.email + "\n" + bills.mkString(" "))
              case _ => complete(HttpResponse(StatusCodes.NotFound))
            }
          } // api/v1/user/
        } ~ (pathEnd & post & entity(as[UserView])) { user =>
          onSuccess(userService.addUser(user.email, user.phone)) {
            case Some(uid) => complete(uid.toString)
            case None => complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }
  }
}
