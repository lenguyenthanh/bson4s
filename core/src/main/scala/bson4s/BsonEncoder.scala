package bson4s

import cats.syntax.all.*
import cats.Contravariant
import org.bson.codecs.EncoderContext
import org.bson.*

/** A type class that provides a conversion from a value of type `A` to a [[BsonValue]] value. */
trait BsonEncoder[A] extends Serializable:
  self =>

  def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit

  /** Convert a value to BsonValue. */
  def unsafeToBsonValue(a: A): BsonValue = ???

  def safeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Either[Throwable, Unit] =
    Either.catchNonFatal(unsafeBsonEncode(writer, a, encoderContext))

  def safeToBsonValue(a: A): Either[Throwable, BsonValue] =
    Either.catchNonFatal(unsafeToBsonValue(a))

  /** Create a new [[BsonEncoder]] by applying a function to a value of type `B` before encoding as an `A`. */
  final def contramap[B](f: B => A): BsonEncoder[B] =
    new BsonEncoder[B]:
      override def unsafeToBsonValue(b: B): BsonValue =
        self.unsafeToBsonValue(f(b))

      override def unsafeBsonEncode(writer: BsonWriter, b: B, encoderContext: EncoderContext): Unit =
        self.unsafeBsonEncode(writer, f(b), encoderContext)

  /**
   * Create a new [[BsonEncoder]] by applying a function to the output of this one.
   */
  final def mapBsonValue(f: BsonValue => BsonValue): BsonEncoder[A] =
    new BsonEncoder[A]:
      override def unsafeToBsonValue(a: A): BsonValue = f(self.unsafeToBsonValue(a))

      override def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit =
        val bsonValue = unsafeToBsonValue(a)
        bsonValueCodecSingleton.encode(writer, bsonValue, encoderContext)

object BsonEncoder:
  // val bsonValueCodecSingleton: Codec[BsonValue]   = new BsonValueCodec()
  val dummyRoot = "d"

  def apply[A](implicit ev: BsonEncoder[A]): BsonEncoder[A] = ev

  def fastInstance[A](javaEncoder: org.bson.codecs.Encoder[A], toBsonValueOpt: A => BsonValue = null): BsonEncoder[A] =
    new BsonEncoder[A]:
      val cachedToBsonFunc: A => BsonValue =
        if toBsonValueOpt == null then super.unsafeToBsonValue
        else toBsonValueOpt

      override def unsafeToBsonValue(a: A): BsonValue =
        cachedToBsonFunc(a)

      override def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit =
        javaEncoder.encode(writer, a, encoderContext)

  def fastInstance[A](encodeFunc: (BsonWriter, A) => Unit): BsonEncoder[A] =
    new BsonEncoder[A]:
      override def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit =
        encodeFunc(writer, a)

  def slowInstance[A](f: A => BsonValue): BsonEncoder[A] =
    new BsonEncoder[A]:
      override def unsafeToBsonValue(a: A): BsonValue = f(a)

      override def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit =
        bsonValueCodecSingleton.encode(writer, f(a), encoderContext)

  def unsafeEncodeAsBsonDoc[A](a: A)(implicit encA: BsonDocumentEncoder[A]): BsonDocument =
    val doc = new BsonDocument()
    encA.unsafeBsonEncode(new BsonDocumentWriter(doc), a, bsonEncoderContextSingleton)
    doc

  implicit final val bsonEncoderContravariant: Contravariant[BsonEncoder] =
    new Contravariant[BsonEncoder]:
      final def contramap[A, B](e: BsonEncoder[A])(f: B => A): BsonEncoder[B] = e.contramap(f)

trait BsonDocumentEncoder[A] extends BsonEncoder[A]:

  override def unsafeToBsonValue(a: A): BsonDocument = ???
  // super.unsafeToBsonValue(a).asInstanceOf[BsonDocument]

  override def unsafeBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit =
    writer.writeStartDocument()
    unsafeFieldsBsonEncode(writer, a, encoderContext)
    writer.writeEndDocument()

  def unsafeFieldsBsonEncode(writer: BsonWriter, a: A, encoderContext: EncoderContext): Unit

trait JavaEncoder[A] extends org.bson.codecs.Encoder[A]:

  def getEncoderClass: Class[A] = ???
