/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import static gr.codebb.arcadeflex.WIP.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.common.fucPtr.*;

public class _2610intfH {

    public static final int MAX_2610 = (2);

    public static class YM2610interface extends AY8910interface {

        public YM2610interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler, int[] pcmromb, int[] pcmroma, int[] volumeFM) {
            super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw, ym_handler);
            this.pcmromb = pcmromb;
            this.pcmroma = pcmroma;
            this.volumeFM = volumeFM;
        }
        public int[] pcmromb;/* Delta-T rom region */
        public int[] pcmroma;/* ADPCM   rom region */
        public int[] volumeFM;/* use YM3012_VOL macro */

    }

}
