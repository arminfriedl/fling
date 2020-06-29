package net.friedl.fling.model.json;

import java.io.IOException;
import java.nio.file.Path;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PathSerializer extends StdSerializer<Path> {

  public PathSerializer() {
    this(Path.class);
  }

  protected PathSerializer(Class<Path> t) {
    super(t);
  }

  private static final long serialVersionUID = -1003917305429893614L;

  @Override
  public void serialize(Path value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.toString());
  }

}
