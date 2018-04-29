package ru.fintech.school.ubilling.handler

import java.sql.Timestamp
import java.util.UUID

import ru.fintech.school.ubilling.dao.{BillDao, BillItemsDao}
import ru.fintech.school.ubilling.domain.{BillView, BillViewItem}
import ru.fintech.school.ubilling.domain.BillResponse._
import ru.fintech.school.ubilling.schema.TableDefinitions.{Bill, BillId, BillItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BillService {
  def findBill(billId: BillId): Future[Option[BillView]]

  def addBill(billView: BillView): Future[Option[BillId]]
}

object BillImplicits {
  implicit def viewToTable(billView: BillView)(implicit uuid: BillId): Bill = {
    Bill(uuid, billView.name, billView.date)
  }

  implicit def viewItemToTable(item: BillViewItem)(implicit billId: BillId): BillItem = {
    BillItem(UUID.randomUUID(),
      item.name, item.description,
      item.price, item.count, billId
    )
  }

  implicit def tableToView(bill: Bill, items: Seq[BillItem]): BillView = {
    BillView(
      bill.name,
      items.map(tableItemToView),
      bill.date.toLocalDateTime.toLocalDate
    )
  }

  implicit def tableItemToView(billItem: BillItem): BillViewItem = {
    billItem match {
      case BillItem(_, n, d, p, c, _) => BillViewItem(n, d, p, c)
    }
  }

}

class BillServiceImpl(dao: BillItemsDao with BillDao) extends BillService {
  import BillImplicits._

  override def findBill(billId: BillId): Future[Option[BillView]] = {
    val res = dao.findBill(billId) zip dao.findItems(billId)
    res.map {
      case (Some(a), b) => Some(tableToView(a, b))
      case _ => None
    }
  }

  override def addBill(billView: BillView): Future[Option[BillId]] = {
    implicit val uuid = UUID.randomUUID
    val items = billView.items.map(viewItemToTable)
    val res = dao.addBill(billView) zip dao.addItems(items)
    res.map {
      case (1, _) => Some(uuid)
      case _ => None
    }
  }

}
