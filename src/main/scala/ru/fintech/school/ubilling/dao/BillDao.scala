package ru.fintech.school.ubilling.dao

import java.util.UUID

import ru.fintech.school.ubilling.HasDbConfigProvider
import ru.fintech.school.ubilling.schema.TableDefinitions._

import scala.concurrent.Future

trait BillDao {
  def findBill(id: UUID): Future[Option[Bill]]

  def addBill(bill: Bill): Future[Int]
}

trait BillItemsDao {
  def findItems(billId: UUID): Future[Seq[BillItem]]

  def addItems(items: Seq[BillItem]): Future[Option[Int]]
}

trait RelationalBillDao extends BillDao
  with HasDbConfigProvider
  with BillsTable {

  import profile.api._

  override def findBill(id: UUID): Future[Option[Bill]] = {
    db.run(bills.filter(_.bid === id).result.headOption)
  }

  override def addBill(bill: Bill): Future[Int] = {
    db.run(bills += bill)
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
    users.schema ++ usersBills.schema

  def createSchema = db.run(schema.create)

  def dropSchema = db.run(schema.drop)
}
