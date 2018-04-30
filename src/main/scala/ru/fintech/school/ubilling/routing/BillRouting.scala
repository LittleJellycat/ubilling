package ru.fintech.school.ubilling.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{FutureDirectives, MethodDirectives, PathDirectives, RouteDirectives}
import ru.fintech.school.ubilling.domain.{BillResponse, BillView}
import ru.fintech.school.ubilling.handler.BillService

object BillRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with SprayJsonSupport
  with FutureDirectives {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def route(handler: BillService): Route = {
    pathPrefix("api" / "v1") {
      pathPrefix("bill") {
        get {
          pathPrefix(JavaUUID) { billId =>
            onSuccess(handler.findBill(billId)) {
              case Some(bill) => complete(BillResponse(billId, bill))
              case None => complete(HttpResponse(StatusCodes.NotFound))
            }
          }
        } ~
        post {
          entity(as[BillView]) { bill =>
            onSuccess(handler.addBill(bill)) {
              case Some(billId) => complete(billId.toString)
              case None => complete(HttpResponse(StatusCodes.InternalServerError))
            }

          }
        }
      }
    }
  }
}
