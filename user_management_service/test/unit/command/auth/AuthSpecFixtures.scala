package unit.command.auth

import org.scalatest.TestSuite

import java.io.{BufferedWriter, File, FileWriter}

trait AuthSpecFixtures { self: TestSuite =>
  protected def withFile(
      filename: String,
      content: String
    )(f: => Any
    ): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
    f
    file.delete()
  }
}
