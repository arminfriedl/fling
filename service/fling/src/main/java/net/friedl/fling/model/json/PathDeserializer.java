package net.friedl.fling.model.json;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class PathDeserializer extends StdDeserializer<Path> {
  private static final long serialVersionUID = 1504807365764537418L;

  public PathDeserializer() {
    this(String.class);
  }

  protected PathDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Path deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    ObjectCodec codec = p.getCodec();
    JsonNode node = codec.readTree(p);

    return Paths.get(node.textValue());
  }

}
