package ru.fintech.school.ubilling.mailer

case class Mail(from: String,
                to: Seq[String],
                subject: String,
                message: String) {
  def createMail(positions: Seq[String], total: Double, link: String): String = {
    s"""
       |<ol>
       |${positions.map(p => """<li>$p</li>""")}
       |</ol>
       |Total: $total
       |Pay here: $link
      """.stripMargin
  }
}
