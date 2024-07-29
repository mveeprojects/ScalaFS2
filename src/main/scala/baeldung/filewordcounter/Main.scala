package baeldung.filewordcounter

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2._
import fs2.io.file.{Files, Path}

import java.nio.file.Paths

// https://www.baeldung.com/scala/fs2-functional-streams

object Main extends App {

  def readAndWriteFile(readFrom: String, writeTo: String): Stream[IO, Unit] = {

    val fs2Path = Path.fromNioPath(Paths.get(readFrom))

    val source: Stream[IO, Byte] = Files[IO].readAll(fs2Path)

    val pipe: Pipe[IO, Byte, Byte] = src =>
      src
        .through(text.utf8.decode)
        .through(text.lines)
        .flatMap(line => Stream.apply(line.split("\\W+"): _*))
        .fold(Map.empty[String, Int]) { (count, word) =>
          count + (word -> (count.getOrElse(word, 0) + 1))
        }
        .map(_.foldLeft("") { case (acc, (word, count)) =>
          acc + s"$word = $count\n"
        })
        .through(text.utf8.encode)

    val sink: Pipe[IO, Byte, Unit] = Files[IO].writeAll(Path(writeTo))

    val stream: Stream[IO, Unit] = source.through(pipe).through(sink)

    stream
  }

  readAndWriteFile(
    "src/main/resources/baeldung/filewordcounter/input.txt",
    "src/main/resources/baeldung/filewordcounter/output.txt"
  ).compile.toList.unsafeRunSync()
}
