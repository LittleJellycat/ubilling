package ru.fintech.school.ubilling.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import ru.fintech.school.ubilling.domain.{BillResponse, BillView, UserResponse, UserView}
import ru.fintech.school.ubilling.schema.TableDefinitions.{BillId, User, UserId}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}


trait BillRequestHandler {

  def processPhotoStream(billId: BillId, userId: UserId, source: Source[ByteString, Any]): Future[ToResponseMarshallable]

  def findBill(billId: BillId): Future[ToResponseMarshallable]

  def findBillIds(userId: UserId): Future[ToResponseMarshallable]

  def addBill(bill: BillView): Future[ToResponseMarshallable]

  def getPhoto(billId: BillId): Future[ToResponseMarshallable]

  def addPhoto(billId: BillId, photo: Array[Byte]): Future[ToResponseMarshallable]
}

trait UserRequestHandler {

  def addUser(user: UserView): Future[ToResponseMarshallable]

  def findUser(userId: UserId): Future[ToResponseMarshallable]

  def findUserIds(billId: BillId): Future[ToResponseMarshallable]
}

trait RequestHandler extends BillRequestHandler with UserRequestHandler {

  def assignBill(billId: BillId, userId: UserId): Future[ToResponseMarshallable]
}

class RequestHandlerImpl(
  userService: UserService,
  billService: BillService,
  userBillService: UserBillService
)(implicit val ec: ExecutionContext, am: ActorMaterializer) extends RequestHandler with SprayJsonSupport {

  import BillResponse._

  override def findBill(billId: BillId): Future[ToResponseMarshallable] = {
    billService.findBill(billId).map {
      case Some(bill) => BillResponse(billId, bill)
      case _ => HttpResponse(StatusCodes.NotFound)
    }
  }

  override def addBill(bill: BillView) = {
    billService.addBill(bill).map {
      case Some(billId) => billId.toString
      case None => HttpResponse(StatusCodes.InternalServerError)
    }
  }

  override def findUserIds(billId: BillId): Future[ToResponseMarshallable] = {
    userBillService.findUserIds(billId).map(_.toString)
  }

  override def findUser(userId: UserId): Future[ToResponseMarshallable] = {
    userService.findUser(userId).map {
      case Some(User(_, email, phone)) =>
        UserResponse(userId, UserView(email, phone))
      case _ => HttpResponse(StatusCodes.NotFound)
    }
  }

  override def addUser(user: UserView): Future[ToResponseMarshallable] = {
    userService.addUser(user.email, user.phone) map {
      case Right(uid) => JsObject("uid" -> JsNumber(uid))
      case Left(thr) => JsObject("err" -> JsString(thr.getMessage))
    }
  }

  override def findBillIds(userId: UserId): Future[ToResponseMarshallable] = {
    userBillService.findBillIds(userId).map(_.toString)
  }

  override def assignBill(
    billId: BillId, userId: UserId
  ): Future[ToResponseMarshallable] = {
    userBillService.assignBill(userId, billId).map { res =>
      HttpResponse(if (res) StatusCodes.OK else StatusCodes.BadRequest)
    }
  }

  override def addPhoto(
    billId: BillId, photo: Array[Byte]
  ): Future[ToResponseMarshallable] = {
    //TODO: error handling
    billService.addPhoto(billId, photo).map { res =>
      HttpResponse(
        if (res) StatusCodes.OK else StatusCodes.InternalServerError
      )
    }
  }

  override def getPhoto(
    billId: BillId
  ): Future[ToResponseMarshallable] = {
    billService.getPhoto(billId).map {
      case Some(photo) =>
        val entity = HttpEntity.Strict(
          MediaTypes.`application/octet-stream`,
          ByteString(photo)
        )
        HttpResponse(StatusCodes.OK, entity = entity)
      case None => HttpResponse(StatusCodes.NotFound)
    }
  }

  override def processPhotoStream(
    billId: BillId,
    userId: UserId,
    source: Source[ByteString, Any]
  ): Future[ToResponseMarshallable] = {
    val flow = Framing.delimiter(
      ByteString("\n"), 1000, allowTruncation = true
    )
    source.via(flow)
      .runFold(Array.emptyByteArray)((a, b) => a ++ b.toArray[Byte])
      .map(addPhoto(billId, _))
  }
}
