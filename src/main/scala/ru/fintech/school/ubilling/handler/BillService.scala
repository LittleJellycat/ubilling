package ru.fintech.school.ubilling.handler

import java.util.UUID

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import ru.fintech.school.ubilling.dao.{BillDao, BillItemsDao}
import ru.fintech.school.ubilling.domain.BillResponse._
import ru.fintech.school.ubilling.domain.{BillView, BillViewItem}
import ru.fintech.school.ubilling.schema.TableDefinitions.{Bill, BillId, BillItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BillService {
  def findBill(billId: BillId): Future[Option[BillView]]

  def addBill(billView: BillView): Future[Option[BillId]]

  def getPhoto(billId: BillId): Future[Option[Array[Byte]]]

  def addPhoto(billId: BillId, photo: Array[Byte]): Future[Boolean]
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

class BillServiceImpl(dao: BillItemsDao with BillDao)
  (implicit val materializer: ActorMaterializer) extends BillService {

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

  override def addPhoto(
    billId: BillId, photo: Array[Byte]
  ): Future[Boolean] = {
    // TODO: compression/validation
    dao.addPhoto(billId, photo)
  }

  override def getPhoto(
    billId: BillId
  ): Future[Option[Array[Byte]]] = {
    dao.getPhoto(billId).runWith(Sink.headOption)
  }
}