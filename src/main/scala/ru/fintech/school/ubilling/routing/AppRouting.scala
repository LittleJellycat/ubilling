package ru.fintech.school.ubilling.routing

import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.UUID

import akka.NotUsed
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives._
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.util.ByteString
import ru.fintech.school.ubilling.domain.UserResponse._
import ru.fintech.school.ubilling.domain._
import ru.fintech.school.ubilling.handler.{BillService, RequestHandler, UserBillService, UserService}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object AppRouting extends RouteDirectives
  with PathDirectives
  with MethodDirectives
  with FutureDirectives
  with FileUploadDirectives
  with CodingDirectives
  with SprayJsonSupport {

  import ru.fintech.school.ubilling.domain.BillResponse._

  def route(handler: RequestHandler): Route = {
    pathPrefix("api" / "v1") {
      path("upload" / "bill" / JavaUUID) { billId =>
        decodeRequest {
          (post & fileUpload("img")) { case (meta, src) =>
            val uid = 0L // TODO
            complete(handler.processPhotoStream(billId, uid, src))
          }
        }
      } ~
        pathPrefix("bill") {
          // api/v1/bill/{bill-id}
          (get & path(JavaUUID)) { billId =>
            complete(handler.findBill(billId))
          } ~ // api/v1/bill/{bill-id}/users
            (get & path(JavaUUID / "users")) { billId =>
              complete(handler.findUserIds(billId))
            } ~ // api/v1/bill/
            (post & pathEnd & entity(as[BillView])) { bill =>
              complete(handler.addBill(bill))
            } ~ //api/v1/bill/photo
            (get & path(JavaUUID / "photo")) { billId =>
              complete(handler.getPhoto(billId))
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

