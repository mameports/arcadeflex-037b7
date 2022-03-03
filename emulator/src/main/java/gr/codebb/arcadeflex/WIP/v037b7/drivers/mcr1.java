/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b7.drivers;

import static arcadeflex.v037b7.machine.mcr.*;
import static gr.codebb.arcadeflex.WIP.v037b7.machine.z80fmly.*;
import static gr.codebb.arcadeflex.WIP.v037b7.vidhrdw.mcr12.*;
import static arcadeflex.common.ptrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.old.arcadeflex.fileio.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.generic.funcPtr.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v037b7.sndhrdw.mcr.*;
import static arcadeflex.v037b7.sndhrdw.mcrH.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class mcr1 {

    static int[] nvram_init;

    /**
     * ***********************************
     *
     * Kick input ports
     *
     ************************************
     */
    public static ReadHandlerPtr kick_dial_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(1) & 0x0f) | ((readinputport(6) << 4) & 0xf0);
        }
    };

    /**
     * ***********************************
     *
     * Solar Fox input ports
     *
     ************************************
     */
    public static ReadHandlerPtr solarfox_input_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* This is a kludge; according to the wiring diagram, the player 2 */
 /* controls are hooked up as documented below. If you go into test */
 /* mode, they will respond. However, if you try it in a 2-player   */
 /* game in cocktail mode, they don't work at all. So we fake-mux   */
 /* the controls through player 1's ports */
            if (mcr_cocktail_flip != 0) {
                return readinputport(0) | 0x08;
            } else {
                return ((readinputport(0) & ~0x14) | 0x08) | ((readinputport(0) & 0x08) >> 1) | ((readinputport(2) & 0x01) << 4);
            }
        }
    };

    public static ReadHandlerPtr solarfox_input_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /*  same deal as above */
            if (mcr_cocktail_flip != 0) {
                return readinputport(1) | 0xf0;
            } else {
                return (readinputport(1) >> 4) | 0xf0;
            }
        }
    };

    /**
     * ***********************************
     *
     * NVRAM save/load
     *
     ************************************
     */
    public static nvramPtr mcr1_nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            UBytePtr ram = memory_region(REGION_CPU1);

            if (read_or_write != 0) {
                osd_fwrite(file, ram, 0x7000, 0x800);
            } else if (file != null) {
                osd_fread(file, ram, 0x7000, 0x800);
            } else if (nvram_init != null) {
                memcpy(ram, 0x7000, nvram_init, 16);
            }
        }
    };

    /**
     * ***********************************
     *
     * Main CPU memory handlers
     *
     ************************************
     */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x6fff, MRA_ROM),
                new MemoryReadAddress(0x7000, 0x77ff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf1ff, MRA_RAM),
                new MemoryReadAddress(0xfc00, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x6fff, MWA_ROM),
                new MemoryWriteAddress(0x7000, 0x77ff, MWA_RAM),
                new MemoryWriteAddress(0xf000, 0xf1ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xf400, 0xf41f, paletteram_xxxxRRRRBBBBGGGG_split1_w, paletteram),
                new MemoryWriteAddress(0xf800, 0xf81f, paletteram_xxxxRRRRBBBBGGGG_split2_w, paletteram_2),
                new MemoryWriteAddress(0xfc00, 0xffff, mcr1_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, input_port_3_r),
                new IOReadPort(0x04, 0x04, input_port_4_r),
                new IOReadPort(0x07, 0x07, ssio_status_r),
                new IOReadPort(0x10, 0x10, input_port_0_r),
                new IOReadPort(0xf0, 0xf3, z80ctc_0_r),
                new IOReadPort(-1)
            };

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x1c, 0x1f, ssio_data_w),
                new IOWritePort(0xe0, 0xe0, watchdog_reset_w),
                new IOWritePort(0xe8, 0xe8, MWA_NOP),
                new IOWritePort(0xf0, 0xf3, z80ctc_0_w),
                new IOWritePort(-1) /* end of table */};

    /**
     * ***********************************
     *
     * Port definitions
     *
     ************************************
     */
    static InputPortPtr input_ports_solarfox = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xfe, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 -- dipswitches */
            PORT_DIPNAME(0x03, 0x03, "Bonus");
            PORT_DIPSETTING(0x02, "None");
            PORT_DIPSETTING(0x03, "After 10 racks");
            PORT_DIPSETTING(0x01, "After 20 racks");
            PORT_BIT(0x0c, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x40, 0x40, "Ignore Hardware Failure");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* IN4 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* AIN0 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_kick = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* IN1 -- this is the Kick spinner input.  */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_REVERSE, 3, 50, 0, 0);

            PORT_START();
            /* IN2 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 -- dipswitches */
            PORT_DIPNAME(0x01, 0x00, "Music");
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xfe, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN4 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* AIN0 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* (fake) player 2 dial */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_kicka = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* IN1 -- this is the Kick spinner input.  */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_REVERSE, 3, 50, 0, 0);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xfe, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 -- dipswitches */
            PORT_DIPNAME(0x01, 0x00, "Music");
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x3e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN4 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* AIN0 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* (fake) player 2 dial */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_REVERSE | IPF_COCKTAIL, 3, 50, 0, 0);
            INPUT_PORTS_END();
        }
    };

    /**
     * ***********************************
     *
     * Graphics definitions
     *
     ************************************
     */
    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, mcr_bg_layout, 0, 1), /* colors 0-15 */
                new GfxDecodeInfo(REGION_GFX2, 0, mcr_sprite_layout, 16, 1), /* colors 16-31 */
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ***********************************
     *
     * Machine driver
     *
     ************************************
     */
    static MachineDriver machine_driver_mcr1 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        2500000, /* 2.5 MHz */
                        readmem, writemem, readport, writeport,
                        mcr_interrupt, 1,
                        null, 0, mcr_daisy_chain
                ),
                SOUND_CPU_SSIO
            },
            30, DEFAULT_REAL_30HZ_VBLANK_DURATION,
            1,
            mcr_init_machine,
            /* video hardware */
            32 * 16, 30 * 16, new rectangle(0 * 16, 32 * 16 - 1, 0 * 16, 30 * 16 - 1),
            gfxdecodeinfo,
            32, 32,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            null,
            mcr12_vh_start,
            mcr12_vh_stop,
            mcr1_vh_screenrefresh,
            /* sound hardware */
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                SOUND_SSIO
            },
            mcr1_nvram_handler
    );

    /**
     * ***********************************
     *
     * Driver initialization
     *
     ************************************
     */
    static int hiscore_init[] = {0, 0, 1, 1, 1, 1, 1, 3, 3, 3, 7};
    public static InitDriverPtr init_solarfox = new InitDriverPtr() {
        public void handler() {

            nvram_init = hiscore_init;

            MCR_CONFIGURE_SOUND(MCR_SSIO);
            install_port_read_handler(0, 0x00, 0x00, solarfox_input_0_r);
            install_port_read_handler(0, 0x01, 0x01, solarfox_input_1_r);
            install_port_write_handler(0, 0x01, 0x01, mcr_control_port_w);

            mcr12_sprite_xoffs = 16;
            mcr12_sprite_xoffs_flip = 0;
        }
    };

    public static InitDriverPtr init_kick = new InitDriverPtr() {
        public void handler() {
            nvram_init = null;

            MCR_CONFIGURE_SOUND(MCR_SSIO);
            install_port_read_handler(0, 0x01, 0x01, kick_dial_r);
            install_port_write_handler(0, 0x03, 0x03, mcr_control_port_w);

            mcr12_sprite_xoffs = 0;
            mcr12_sprite_xoffs_flip = 16;
        }
    };

    /**
     * ***********************************
     *
     * ROM definitions
     *
     ************************************
     */
    static RomLoadPtr rom_solarfox = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sfcpu.3b", 0x0000, 0x1000, 0x8c40f6eb);
            ROM_LOAD("sfcpu.4b", 0x1000, 0x1000, 0x4d47bd7e);
            ROM_LOAD("sfcpu.5b", 0x2000, 0x1000, 0xb52c3bd5);
            ROM_LOAD("sfcpu.4d", 0x3000, 0x1000, 0xbd5d25ba);
            ROM_LOAD("sfcpu.5d", 0x4000, 0x1000, 0xdd57d817);
            ROM_LOAD("sfcpu.6d", 0x5000, 0x1000, 0xbd993cd9);
            ROM_LOAD("sfcpu.7d", 0x6000, 0x1000, 0x8ad8731d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("sfsnd.7a", 0x0000, 0x1000, 0xcdecf83a);
            ROM_LOAD("sfsnd.8a", 0x1000, 0x1000, 0xcb7788cb);
            ROM_LOAD("sfsnd.9a", 0x2000, 0x1000, 0x304896ce);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sfcpu.4g", 0x0000, 0x1000, 0xba019a60);
            ROM_LOAD("sfcpu.5g", 0x1000, 0x1000, 0x7ff0364e);

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sfvid.1a", 0x0000, 0x2000, 0x9d9b5d7e);
            ROM_LOAD("sfvid.1b", 0x2000, 0x2000, 0x78801e83);
            ROM_LOAD("sfvid.1d", 0x4000, 0x2000, 0x4d8445cf);
            ROM_LOAD("sfvid.1e", 0x6000, 0x2000, 0x3da25495);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kick = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1200a-v2.b3", 0x0000, 0x1000, 0x65924917);
            ROM_LOAD("1300b-v2.b4", 0x1000, 0x1000, 0x27929f52);
            ROM_LOAD("1400c-v2.b5", 0x2000, 0x1000, 0x69107ce6);
            ROM_LOAD("1500d-v2.d4", 0x3000, 0x1000, 0x04a23aa1);
            ROM_LOAD("1600e-v2.d5", 0x4000, 0x1000, 0x1d2834c0);
            ROM_LOAD("1700f-v2.d6", 0x5000, 0x1000, 0xddf84ce1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("4200-a.a7", 0x0000, 0x1000, 0x9e35c02e);
            ROM_LOAD("4300-b.a8", 0x1000, 0x1000, 0xca2b7c28);
            ROM_LOAD("4400-c.a9", 0x2000, 0x1000, 0xd1901551);
            ROM_LOAD("4500-d.a10", 0x3000, 0x1000, 0xd36ddcdc);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("1800g-v2.g4", 0x0000, 0x1000, 0xb4d120f3);
            ROM_LOAD("1900h-v2.g5", 0x1000, 0x1000, 0xc3ba4893);

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2600a-v2.1e", 0x0000, 0x2000, 0x2c5d6b55);
            ROM_LOAD("2700b-v2.1d", 0x2000, 0x2000, 0x565ea97d);
            ROM_LOAD("2800c-v2.1b", 0x4000, 0x2000, 0xf3be56a1);
            ROM_LOAD("2900d-v2.1a", 0x6000, 0x2000, 0x77da795e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kicka = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1200-a.b3", 0x0000, 0x1000, 0x22fa42ed);
            ROM_LOAD("1300-b.b4", 0x1000, 0x1000, 0xafaca819);
            ROM_LOAD("1400-c.b5", 0x2000, 0x1000, 0x6054ee56);
            ROM_LOAD("1500-d.d4", 0x3000, 0x1000, 0x263af0f3);
            ROM_LOAD("1600-e.d5", 0x4000, 0x1000, 0xeaaa78a7);
            ROM_LOAD("1700-f.d6", 0x5000, 0x1000, 0xc06c880f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("4200-a.a7", 0x0000, 0x1000, 0x9e35c02e);
            ROM_LOAD("4300-b.a8", 0x1000, 0x1000, 0xca2b7c28);
            ROM_LOAD("4400-c.a9", 0x2000, 0x1000, 0xd1901551);
            ROM_LOAD("4500-d.a10", 0x3000, 0x1000, 0xd36ddcdc);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("1000-g.g4", 0x0000, 0x1000, 0xacdae4f6);
            ROM_LOAD("1100-h.g5", 0x1000, 0x1000, 0xdbb18c96);

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2600-a.1e", 0x0000, 0x2000, 0x74b409d7);
            ROM_LOAD("2700-b.1d", 0x2000, 0x2000, 0x78eda36c);
            ROM_LOAD("2800-c.1b", 0x4000, 0x2000, 0xc93e0170);
            ROM_LOAD("2900-d.1a", 0x6000, 0x2000, 0x91e59383);
            ROM_END();
        }
    };

    /**
     * ***********************************
     *
     * Game drivers
     *
     ************************************
     */
    public static GameDriver driver_solarfox = new GameDriver("1981", "solarfox", "mcr1.java", rom_solarfox, null, machine_driver_mcr1, input_ports_solarfox, init_solarfox, ROT90 ^ ORIENTATION_FLIP_Y, "Bally Midway", "Solar Fox");
    public static GameDriver driver_kick = new GameDriver("1981", "kick", "mcr1.java", rom_kick, null, machine_driver_mcr1, input_ports_kick, init_kick, ORIENTATION_SWAP_XY, "Midway", "Kick (upright)");
    public static GameDriver driver_kicka = new GameDriver("1981", "kicka", "mcr1.java", rom_kicka, driver_kick, machine_driver_mcr1, input_ports_kicka, init_kick, ROT90, "Midway", "Kick (cocktail)");
}
