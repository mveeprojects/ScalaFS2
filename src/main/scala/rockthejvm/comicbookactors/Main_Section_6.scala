package rockthejvm.comicbookactors

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Chunk, Pull, Pure, Stream}
import rockthejvm.comicbookactors.Data._
import rockthejvm.comicbookactors.Main_Section_2.avengersActors
import rockthejvm.comicbookactors.Model.Actor

// https://blog.rockthejvm.com/fs2/

object Main_Section_6 extends IOApp {

  // Stream[+F[_], +O]
  // Pull[+F[_], +O, +R] -> extra type parameter "R"

  // The Pull type represents the stream as a head and a tail, like a list.
  // The result R represents the information available after the emission of the element of type O
  // that should be used to emit the next value of a stream.
  // For this reason, using Pull directly means to develop recursive programs.
  // R represents all the information we need to calculate the tail of the stream.

  val tomHollandActorPull: Pull[Pure, Actor, Unit] = Pull.output1(tomHolland)

  // A Pull with a return of type Unit can be easily converted into a Stream.
  // Pull that returns Unit is like a List with a head and empty tail.
  val tomHollandActorPullToStream: Stream[Pure, Actor] = tomHollandActorPull.stream

  // Sequence of Pull
  val spiderMenActorPull: Pull[Pure, Actor, Unit] =
    tomHollandActorPull >> Pull.output1(tobeyMaguire) >> Pull.output1(andrewGarfield)

  // Convert Stream into Pull using echo
  val avengersActorsPull: Pull[Pure, Actor, Unit] = avengersActors.pull.echo

  // Convert Stream into Pull using uncons
  // The returned value is an Option because the Stream may be empty:
  // If there is no more value in the original Stream, we will have a None.
  // Otherwise, we will have the head of the stream as a Chunk and a Stream
  // representing the tail of the original stream.
  val unconsAvengersActors: Pull[Pure, Nothing, Option[(Chunk[Actor], Stream[Pure, Actor])]] =
    avengersActors.pull.uncons

  // A variant of the original uncons method returns not the first Chunk
  // but the first stream element. It’s called uncons1:
  val uncons1AvengersActors: Pull[Pure, Nothing, Option[(Actor, Stream[Pure, Actor])]] =
    avengersActors.pull.uncons1

  override def run(args: List[String]): IO[ExitCode] =
    ???
}
