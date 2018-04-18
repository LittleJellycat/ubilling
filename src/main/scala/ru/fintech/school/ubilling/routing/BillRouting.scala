package ru.fintech.school.ubilling.routing

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives
import akka.http.scaladsl.server.directives.MethodDirectives
import akka.http.scaladsl.server.directives.PathDirectives
import akka.http.scaladsl.server.directives.RouteDirectives
import ru.fintech.school.ubilling.domain.Bill
import ru.fintech.school.ubilling.domain.BillResponse
import ru.fintech.school.ubilling.handler.BillService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BillRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with SprayJsonSupport
  with FutureDirectives {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def route(handler: BillService): Route = {
    pathPrefix("api" / "v1") {
      pathPrefix("bill") {
        path(JavaUUID) { billId =>
          onSuccess(handler.findBill(billId)) {
              case Some(bill) => complete(BillResponse(billId, bill))
              case None => complete(HttpResponse(StatusCodes.NotFound))
            }
          }
        }
      }
    }
}
