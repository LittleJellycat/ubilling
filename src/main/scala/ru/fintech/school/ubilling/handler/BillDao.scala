package ru.fintech.school.ubilling.handler

import java.time.LocalDate
import java.util.UUID

import ru.fintech.school.ubilling.domain.Bill
import ru.fintech.school.ubilling.domain.BillItem

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

trait BillDao {
  def findBill(id: UUID): Future[Option[Bill]]
}

object InMemoryBillDao extends BillDao {
  private val storage = TrieMap[UUID, Bill]()

 def findBill(id: UUID): Future[Option[Bill]] = {
   if (id == UUID.fromString("550e8400-e29b-41d4-a716-446655440000")) {
     Future.successful {
       Some(Bill("Теремок", Seq(BillItem("Блинчик сытный", None, 150, 2)), LocalDate.of(2018, 4, 20))) // TODO for test
     }
   } else {
     Future.successful(storage.get(id))
   }
 }
}