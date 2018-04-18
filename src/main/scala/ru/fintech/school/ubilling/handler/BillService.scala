package ru.fintech.school.ubilling.handler

import java.util.UUID

import ru.fintech.school.ubilling.domain.Bill

import scala.concurrent.Future

trait BillService {
  def findBill(billId: UUID): Future[Option[Bill]]
}


class BillServiceImpl(billDao: BillDao) extends BillService {
  override def findBill(billId: UUID): Future[Option[Bill]] = billDao.findBill(billId)
}


