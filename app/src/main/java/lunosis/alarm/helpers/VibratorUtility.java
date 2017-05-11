package lunosis.alarm.helpers;

import java.util.ArrayList;

public final class VibratorUtility {
    public static long[] getSequence(long startIntensity, long endIntensity, long onTime, long offTime, int totalTime)
    {
        ArrayList<Long> sequence = new ArrayList<>();
        sequence.add((long) 0);

        long currentTime = 0;
        startIntensity = (long) MathHelper.clamp(startIntensity, 0, 100);
        endIntensity = (long) MathHelper.clamp(endIntensity, 0, 100);

        long roundedOnTime = Math.round(onTime / 100f) * 100;
        totalTime *= 1000;
        while(currentTime < totalTime)
        {
            float leftOver = 0;
            for (int i = 0; i < roundedOnTime / 100; i++)
            {
                int intensity = Math.round(MathHelper.lerp(startIntensity, endIntensity, (float)currentTime / (float)totalTime));
                if(intensity != 100) {
                    sequence.add((long) intensity);
                    currentTime += intensity;
                    if (i != (roundedOnTime / 100) - 1) {
                        sequence.add((long) (100 - intensity));
                        currentTime += 100 - intensity;
                    } else {
                        leftOver = 100 - intensity;
                    }
                }
                else
                {
                    sequence.add(roundedOnTime);
                    currentTime += roundedOnTime;
                    break;
                }
            }

            sequence.add((long) (offTime + leftOver));
            currentTime += offTime+ leftOver;
        }

        long[] longArr = new long[sequence.size()];
        for (int i = 0; i < sequence.size(); i++)
        {
            longArr[i] = sequence.get(i);
        }
        return longArr;
    }

}
