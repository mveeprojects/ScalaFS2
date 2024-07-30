package rockthejvm.comicbookactors

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import rockthejvm.comicbookactors.Main_Section_4.savedJLActors

// https://blog.rockthejvm.com/fs2/#5-resource-management

object Main_Section_5 extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val acquire = IO {
      val conn = DatabaseConnection("jlaConnection")
      println(s"Acquiring connection to database: $conn")
      conn
    }

    val release = (conn: DatabaseConnection) => IO.println(s"Releasing connection to database: $conn")

    val managedActors: Stream[IO, Int] =
      Stream.bracket(acquire)(release).flatMap(_ => savedJLActors)

    managedActors.compile.drain.as(ExitCode.Success)
  }
}

case class DatabaseConnection(connection: String) extends AnyVal
