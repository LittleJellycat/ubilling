package ru.fintech.school.ubilling.handler

import ru.fintech.school.ubilling.dao.UserBillDao
import ru.fintech.school.ubilling.schema.TableDefinitions.{BillId, UserId}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait UserBillService {
  def assignBill(userId: UserId, billId: BillId): Future[Boolean]

  def findUserIds(billId: BillId): Future[Seq[UserId]]

  def findBillIds(userId: UserId): Future[Seq[BillId]]
}

class UserBillServiceImpl(dao: UserBillDao) extends UserBillService {
  override def assignBill(userId: UserId, billId: BillId): Future[Boolean] = {
    dao.assignBill(userId, billId)
  }

  override def findUserIds(billId: BillId): Future[Seq[UserId]] = {
    dao.findUserIds(billId)
  }

  override def findBillIds(userId: UserId): Future[Seq[BillId]] = {
    dao.findBillIds(userId)
  }
}
