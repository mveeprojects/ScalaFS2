package rockthejvm.comicbookactors

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Chunk, Pure, Stream}
import rockthejvm.comicbookactors.Data._
import rockthejvm.comicbookactors.Model.Actor

// https://blog.rockthejvm.com/fs2/
// https://www.baeldung.com/scala/variances

object Main_Section_2 extends IOApp {

  // "using the Pure effect means that pulling the elements from the stream cannot fail."
  val jlActors: Stream[Pure, Actor] = Stream(
    henryCavil,
    galGodot,
    ezraMiller,
    benFisher,
    rayHardy,
    jasonMomoa
  )

  // single-element pure stream, using "emit" Smart Constructor
  val tomHollandStream: Stream[Pure, Actor] = Stream.emit(tomHolland)

  // multi-element pure stream, using "emits" SC
  val spiderMen: Stream[Pure, Actor] = Stream.emits(
    List(
      tomHolland,
      tobeyMaguire,
      andrewGarfield
    )
  )

  // pure streams can be converted directly into List or Vector.
  val jlActorList: List[Actor]     = jlActors.toList
  val jlActorVector: Vector[Actor] = jlActors.toVector

  // infinite streams
  val infiniteJLActors: Stream[Pure, Actor] = jlActors.repeat
  val repeatedJLActorsList: List[Actor]     = infiniteJLActors.take(12).toList

  // "However, the Pure effect is not sufficient to pull new elements from a stream most of the time.
  // In detail, the operation can fail, or it must interact with some external resource or with some code performing side effects.
  // In this case, we need to use some effect library, such as Cats-effect, and its effect type, called IO[A]."

  // "Covariance is a concept that is very straightforward to understand.
  // We say that a type constructor F[_] is covariant if B is a subtype of type A and F[B] is a subtype of type F[A].
  // In Scala, we declare a covariant type constructor using the notation F[+T]"
  // "Covariance in F means that Pure <: IO => Stream[Pure, O] <: Stream[IO, O]"
  val liftedJLActors: Stream[IO, Actor] = jlActors.covary[IO]

  val savingTomHolland: Stream[IO, Unit] = Stream.eval {
    IO {
      println(s"Saving Actor $tomHolland")
      Thread.sleep(1000)
      println("Finished")
    }
  }

  // Can't use the above directly by appending .toList for example.
  // We need to use ".compile" to create a single instance of the effect.
  // ".drain" is then used to discard any effect output.
  val compiledStream = savingTomHolland.compile.drain

  // We can transform a compiled stream into a list if we like though
  val jlActorsEffectfulList: IO[List[Actor]]      = liftedJLActors.compile.toList
  val compiledEffectfulTomHolland: IO[List[Unit]] = savingTomHolland.compile.toList

  // Can run our effect either by using unsafeRunSync (not ideal) e.g.
  //   import cats.effect.unsafe.implicits.global
  //   savingTomHolland.compile.drain.unsafeRunSync()
  // Or by running as an IOApp (preferred) (extends IOApp, override def run) e.g. [see below and object definition at top of file]
  override def run(args: List[String]): IO[ExitCode] =
    savingTomHolland.compile.drain.as(ExitCode.Success)

  // other SCs available for Seq, Option, Queue, JavaList etc.
  val avengersActors: Stream[Pure, Actor] = Stream.chunk(Chunk.array(Array(
    scarlettJohansson,
    robertDowneyJr,
    chrisEvans,
    markRuffalo,
    chrisHemsworth,
    jeremyRenner
  )))
}
