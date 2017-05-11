package lunosis.alarm.helpers;

public class MathHelper {
    public static float lerp(long a, long b, float t)
    {
        return a + ((b - a) * clamp(t, 0, 1));
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
