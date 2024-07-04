package bson4s

import org.bson.codecs.*
import org.bson.*

private[bson4s] val bsonValueCodecSingleton: Codec[BsonValue] = new BsonValueCodec()

private[bson4s] val bsonEncoderContextSingleton: EncoderContext = EncoderContext.builder().build()
private[bson4s] val bsonDecoderContextSingleton: DecoderContext = DecoderContext.builder().build()
