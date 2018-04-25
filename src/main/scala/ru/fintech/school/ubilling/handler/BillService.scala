package ru.fintech.school.ubilling.handler

import java.util.UUID

import ru.fintech.school.ubilling.dao.{BillDao, BillItemsDao}
import ru.fintech.school.ubilling.domain.{BillView, BillViewItem}
import ru.fintech.school.ubilling.schema.TableDefinitions.{Bill, BillItem}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BillService {
  def findBill(billId: UUID): Future[Option[BillView]]
}

class BillServiceImpl(dao: BillItemsDao with BillDao) extends BillService {
  override def findBill(billId: UUID): Future[Option[BillView]] = {
    for {
      bill <- dao.findBill(billId)
      items <- dao.findItems(billId)
    } yield {
      bill.map { case Bill(_, name, date) => BillView(
        name,
        items.map { case BillItem(_, n, d, p, c, _) => BillViewItem(n, d, p, c) },
        date.toLocalDateTime.toLocalDate
      )
      }
    }
  }
}

