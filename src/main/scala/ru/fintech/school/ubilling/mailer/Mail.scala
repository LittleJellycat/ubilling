package ru.fintech.school.ubilling.mailer

case class Mail(to: String,
                subject: String,
                message: String)

object Mail {
  def createTotalMail(positions: Seq[String], total: Double, link: String)(to: String, subject: String): Mail = {
    Mail(to, subject,
      s"""
         |<ol>
         |${positions.map(p => """<li>$p</li>""")}
         |</ol>
         |Total: $total
         |Pay here: $link
      """.stripMargin)
  }
}
