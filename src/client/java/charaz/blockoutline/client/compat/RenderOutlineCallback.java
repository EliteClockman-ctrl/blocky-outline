package charaz.blockoutline.client.compat;

@FunctionalInterface
public interface RenderOutlineCallback {
    boolean onBeforeBlockOutline(Object context, Object targetObj);
}
