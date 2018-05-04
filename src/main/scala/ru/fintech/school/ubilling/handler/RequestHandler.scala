package ru.fintech.school.ubilling.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import ru.fintech.school.ubilling.domain.{BillResponse, BillView, UserResponse, UserView}
import ru.fintech.school.ubilling.schema.TableDefinitions.{BillId, User, UserId}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}


trait RequestHandler {
  def findBill(billId: BillId): Future[ToResponseMarshallable]

  def addBill(bill: BillView): Future[ToResponseMarshallable]

  def findUserIds(billId: BillId): Future[ToResponseMarshallable]

  def findUser(userId: UserId): Future[ToResponseMarshallable]

  def addUser(user: UserView): Future[ToResponseMarshallable]

  def findBillIds(userId: UserId): Future[ToResponseMarshallable]

  def assignBill(billId: BillId, userId: UserId): Future[ToResponseMarshallable]

}

class RequestHandlerImpl(
  userService: UserService,
  billService: BillService,
  userBillService: UserBillService
)(implicit val ec: ExecutionContext) extends RequestHandler with SprayJsonSupport {

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
}
