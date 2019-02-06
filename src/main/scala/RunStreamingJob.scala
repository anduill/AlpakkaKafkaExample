import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import scala.concurrent.duration._

object RunStreamingJob extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def processRecord(record: ConsumerRecord[String, String]): IO[(String, String)] =
      IO.pure(record.key -> record.value)

    val consumerSettings =
      ConsumerSettings(
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new StringDeserializer
      )
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group-1")

    val producerSettings =
      ProducerSettings(
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer
      )
        .withBootstrapServers("localhost:9092")

    val stream =
      producerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          consumerStream[IO]
            .using(consumerSettings)
            .evalTap(_.subscribeTo("topic-1"))
            .flatMap(_.stream)
            .mapAsync(25) { message =>
              processRecord(message.record)
                .map { case (key, value) =>
                  val record = ProducerRecord("topic-2", key, value)
                  ProducerMessage.one(record, message.committableOffset)
                }
            }
            .evalMap(producer.producePassthrough)
            .through(commitBatchWithinF(500, 15.seconds))
        }

    stream.compile.drain.as(ExitCode.Success)
  }
}
