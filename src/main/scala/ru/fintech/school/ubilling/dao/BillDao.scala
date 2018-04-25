package ru.fintech.school.ubilling.dao

import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import ru.fintech.school.ubilling.HasDbConfigProvider
import ru.fintech.school.ubilling.schema.TableDefinitions._
import slick.jdbc.JdbcProfile

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

trait BillDao {
  def findBill(id: UUID): Future[Option[Bill]]
}

trait BillItemsDao {
  def findItems(billId: UUID): Future[Seq[BillItem]]
}

object InMemoryBillDao extends BillDao {
  private val storage = TrieMap[UUID, Bill]()

  def findBill(id: UUID): Future[Option[Bill]] = {
    if (id == UUID.fromString("550e8400-e29b-41d4-a716-446655440000")) {
      Future.successful {
        Some(Bill(id, "Теремок", Timestamp.valueOf(
          LocalDate.of(2018, 4, 20).atStartOfDay()
        )))
      }
      // Seq(BillItem("Блинчик сытный", None, 150, 2)), LocalDate.of(2018, 4, 20))) // TODO for test
    } else {
      Future.successful(storage.get(id))
    }
  }
}

trait RelationalBillDao extends BillDao
  with HasDbConfigProvider
  with BillsTable {

  import profile.api._

  override def findBill(id: UUID): Future[Option[Bill]] = {
    db.run(bills.filter(_.bid === id).result.headOption)
  }
}

trait RelationalBillItemsDao extends BillItemsDao
  with HasDbConfigProvider
  with BillItemsTable {

  import profile.api._

  override def findItems(billId: BillId): Future[Seq[BillItem]] = {
    db.run(billItems.filter(_.bid === billId).result)
  }
}

object BillDAL extends RelationalBillDao
  with RelationalBillItemsDao {

  import profile.api._

  private[this] val schema = bills.schema ++ billItems.schema

  def createSchema = db.run(schema.create)

  def dropSchema = db.run(schema.drop)
}
