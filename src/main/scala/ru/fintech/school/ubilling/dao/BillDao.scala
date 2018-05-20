package ru.fintech.school.ubilling.dao

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import scala.concurrent.Future
import ru.fintech.school.ubilling.HasDbConfigProvider
import ru.fintech.school.ubilling.schema.TableDefinitions._

import scala.concurrent.ExecutionContext.Implicits.global


trait BillDao {

  def findBill(id: BillId): Future[Option[Bill]]

  def addBill(bill: Bill): Future[Int]

  def getPhoto(billId: BillId): Source[Array[Byte], NotUsed]

  def addPhoto(billId: BillId, photo: Array[Byte]): Future[Boolean]

}

trait BillItemsDao {

  def findItems(billId: BillId): Future[Seq[BillItem]]

  def addItems(items: Seq[BillItem]): Future[Option[Int]]
}

trait RelationalBillDao extends BillDao
  with HasDbConfigProvider
  with BillsTable
  with BillsPhotosTable {

  import profile.api._

  override def findBill(id: BillId): Future[Option[Bill]] = {
    db.run(bills.filter(_.bid === id).result.headOption)
  }

  override def addBill(bill: Bill): Future[Int] = {
    db.run(bills += bill)
  }

  override def getPhoto(billId: BillId): Source[Array[Byte], NotUsed] = {
    val p = for (bp <- billsPhotos.filter(_.bid === billId)) yield bp.data
    Source.fromPublisher(db.stream(p.result))
  }

  override def addPhoto(billId: BillId, photo: Array[Byte]) = {
    val photoId = UUID.randomUUID()
    val billPhoto = BillPhoto(billId,
      photoId, Timestamp.valueOf(LocalDateTime.now()),
      photo)
    db.run(billsPhotos += billPhoto).map(_ == 1)
  }
}

trait RelationalBillItemsDao extends BillItemsDao
  with HasDbConfigProvider
  with BillItemsTable {

  import profile.api._

  override def findItems(billId: BillId): Future[Seq[BillItem]] = {
    db.run(billItems.filter(_.bid === billId).result)
  }

  override def addItems(items: Seq[BillItem]): Future[Option[Int]] = {
    db.run(billItems ++= items)
  }
}

object UserBillDAL extends RelationalBillDao
  with RelationalBillItemsDao
  with RelationalUserBillDao {

  import profile.api._

  private[this] val schema = bills.schema ++ billItems.schema ++
    users.schema ++ usersBills.schema ++ billsPhotos.schema

  def createSchema = db.run(schema.create)

  def dropSchema = db.run(schema.drop)
}
