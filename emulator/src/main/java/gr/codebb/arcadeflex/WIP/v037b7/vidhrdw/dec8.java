/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b7.vidhrdw;

import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.mame.Machine;
import arcadeflex.v037b7.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.tilemapH.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;
import arcadeflex.common.ptrLib.UBytePtr;
import static gr.codebb.arcadeflex.old.mame.drawgfx.drawgfx;
import static arcadeflex.v037b7.generic.funcPtr.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;
import static arcadeflex.v037b7.mame.common.memory_region;
import static arcadeflex.v037b7.mame.common.memory_region_length;
import static arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.palette.*;
import static arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.libc.cstring.memset;

public class dec8 {

    static int[] scroll1 = new int[4];
    static int[] scroll2 = new int[4];
    static struct_tilemap dec8_pf0_tilemap, dec8_pf1_tilemap, dec8_fix_tilemap;
    static int[] dec8_pf0_control = new int[0x20];
    static int[] dec8_pf1_control = new int[0x20];
    static int gfx_bank, gfx_mask, game_uses_priority;
    static UBytePtr gfx_base;
    public static UBytePtr dec8_pf0_data = new UBytePtr();
    public static UBytePtr dec8_pf1_data = new UBytePtr();
    public static UBytePtr dec8_row = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Real Ghostbusters has two 1024x8 palette PROM. I don't know the exact
     * values of the resistors between the RAM and the RGB output. I assumed
     * these values (the same as Commando)
     *
     * bit 7 -- 220 ohm resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm
     * resistor -- GREEN -- 2.2kohm resistor -- GREEN -- 220 ohm resistor -- RED
     * -- 470 ohm resistor -- RED -- 1 kohm resistor -- RED bit 0 -- 2.2kohm
     * resistor -- RED
     *
     * bit 7 -- unused -- unused -- unused -- unused -- 220 ohm resistor -- BLUE
     * -- 470 ohm resistor -- BLUE -- 1 kohm resistor -- BLUE bit 0 -- 2.2kohm
     * resistor -- BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr ghostb_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(0) >> 4) & 0x01;
                bit1 = (color_prom.read(0) >> 5) & 0x01;
                bit2 = (color_prom.read(0) >> 6) & 0x01;
                bit3 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr dec8_bac06_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_pf0_control[offset] = data;
        }
    };

    public static WriteHandlerPtr dec8_bac06_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_pf1_control[offset] = data;
        }
    };

    public static WriteHandlerPtr dec8_pf0_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_pf0_data.write(offset, data);
            tilemap_mark_tile_dirty(dec8_pf0_tilemap, offset / 2);
        }
    };

    public static WriteHandlerPtr dec8_pf1_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_pf1_data.write(offset, data);
            tilemap_mark_tile_dirty(dec8_pf1_tilemap, offset / 2);
        }
    };

    public static ReadHandlerPtr dec8_pf0_data_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return dec8_pf0_data.read(offset);
        }
    };

    public static ReadHandlerPtr dec8_pf1_data_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return dec8_pf1_data.read(offset);
        }
    };

    public static WriteHandlerPtr dec8_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            videoram.write(offset, data);
            tilemap_mark_tile_dirty(dec8_fix_tilemap, offset / 2);
        }
    };

    public static WriteHandlerPtr srdarwin_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            videoram.write(offset, data);
            tilemap_mark_tile_dirty(dec8_fix_tilemap, offset);
        }
    };

    public static WriteHandlerPtr dec8_scroll1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll1[offset] = data;
        }
    };

    public static WriteHandlerPtr dec8_scroll2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll2[offset] = data;
        }
    };

    public static WriteHandlerPtr srdarwin_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            switch (offset) {
                case 0:
                    /* Top 3 bits - bank switch, bottom 4 - scroll MSB */
                    bankaddress = 0x10000 + (data >> 5) * 0x4000;
                    cpu_setbank(1, new UBytePtr(RAM, bankaddress));
                    scroll2[0] = data & 0xf;
                    return;

                case 1:
                    scroll2[1] = data;
                    return;
            }
        }
    };

    public static WriteHandlerPtr lastmiss_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* Bottom 4 bits - bank switch, Bits 4 & 5 - Scroll MSBs */
            bankaddress = 0x10000 + (data & 0x0f) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            scroll2[0] = (data >> 5) & 1;
            scroll2[2] = (data >> 6) & 1;

            if (cpu_get_pc() == 0xfa51) {
                cpu_set_reset_line(1, PULSE_LINE); /* No way this can be right... */
            }
            if (cpu_get_pc() == 0xf9d2) {
                cpu_set_reset_line(1, PULSE_LINE); /* No way this can be right... */
            }

            //logerror("PC %06x - Write %02x to %04x\n",cpu_get_pc(),data,offset+0x1802);
        }
    };

    public static WriteHandlerPtr lastmiss_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll2[1] = data;
        }
    };

    public static WriteHandlerPtr lastmiss_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll2[3] = data;
        }
    };

    public static WriteHandlerPtr gondo_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x0:
                    scroll2[1] = data;
                    /* X LSB */
                    break;
                case 0x8:
                    scroll2[3] = data;
                    /* Y LSB */
                    break;
                case 0x10:
                    scroll2[0] = (data >> 0) & 1;
                    /* Bit 0: X MSB */
                    scroll2[2] = (data >> 1) & 1;
                    /* Bit 1: Y MSB */
 /* Bit 2 is also used in Gondo & Garyoret */
                    break;
            }
        }
    };

    /**
     * ***************************************************************************
     */
    /* 'Karnov' sprites, used by Gondomania, Last Mission, Shackled, Ghostbusters */
    static void draw_sprites1(osd_bitmap bitmap, int priority) {
        int offs, x, y, sprite, sprite2, colour, extra, fx, fy;

        for (offs = 0; offs < 0x800; offs += 8) {
            y = buffered_spriteram.read(offs + 1) + (buffered_spriteram.read(offs) << 8);
            if ((y & 0x8000) == 0) {
                continue;
            }

            fx = buffered_spriteram.read(offs + 3);

            if ((fx & 0x1) == 0) {
                continue;
            }

            extra = fx & 0x10;
            fy = fx & 0x2;
            fx = fx & 0x4;

            x = buffered_spriteram.read(offs + 5) + (buffered_spriteram.read(offs + 4) << 8);
            colour = buffered_spriteram.read(offs + 6) >> 4;
            if (priority == 1 && (colour & 8) != 0) {
                continue;
            }
            if (priority == 2 && (colour & 8) == 0) {
                continue;
            }
            sprite = buffered_spriteram.read(offs + 7) + (buffered_spriteram.read(offs + 6) << 8);
            sprite &= 0x0fff;

            if (extra != 0) {
                y = y + 16;
                sprite &= 0xffe;
            }

            x = x & 0x01ff;
            y = y & 0x01ff;
            x = (x + 16) % 0x200;
            y = (y + 16) % 0x200;
            x = 256 - x;
            y = 256 - y;
            if (flip_screen() != 0) {
                y = 240 - y;
                x = 240 - x;
                if (fx != 0) {
                    fx = 0;
                } else {
                    fx = 1;
                }
                if (fy != 0) {
                    fy = 0;
                } else {
                    fy = 1;
                }
                if (extra != 0) {
                    y = y - 16;
                }
            }

            /* Y Flip determines order of multi-sprite */
            if (extra != 0 && fy != 0) {
                sprite2 = sprite;
                sprite++;
            } else {
                sprite2 = sprite + 1;
            }

            drawgfx(bitmap, Machine.gfx[1],
                    sprite,
                    colour, fx, fy, x, y,
                    null, TRANSPARENCY_PEN, 0);

            /* 1 more sprite drawn underneath */
            if (extra != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        sprite2,
                        colour, fx, fy, x, y + 16,
                        null, TRANSPARENCY_PEN, 0);
            }
        }
    }

    /* 'Dec0' sprites, used by Cobra Command, Oscar */
    static void draw_sprites2(osd_bitmap bitmap, int priority) {
        int offs, x, y, sprite, colour, multi, fx, fy, inc, flash, mult;

        /* Sprites */
        for (offs = 0; offs < 0x800; offs += 8) {
            y = buffered_spriteram.read(offs + 1) + (buffered_spriteram.read(offs) << 8);
            if ((y & 0x8000) == 0) {
                continue;
            }
            x = buffered_spriteram.read(offs + 5) + (buffered_spriteram.read(offs + 4) << 8);
            colour = ((x & 0xf000) >> 12);
            flash = x & 0x800;
            if (flash != 0 && (cpu_getcurrentframe() & 1) != 0) {
                continue;
            }

            if (priority == 1 && (colour & 4) != 0) {
                continue;
            }
            if (priority == 2 && (colour & 4) == 0) {
                continue;
            }

            fx = y & 0x2000;
            fy = y & 0x4000;
            multi = (1 << ((y & 0x1800) >> 11)) - 1;
            /* 1x, 2x, 4x, 8x height */

 /* multi = 0   1   3   7 */
            sprite = buffered_spriteram.read(offs + 3) + (buffered_spriteram.read(offs + 2) << 8);
            sprite &= 0x0fff;

            x = x & 0x01ff;
            y = y & 0x01ff;
            if (x >= 256) {
                x -= 512;
            }
            if (y >= 256) {
                y -= 512;
            }
            x = 240 - x;
            y = 240 - y;

            sprite &= ~multi;
            if (fy != 0) {
                inc = -1;
            } else {
                sprite += multi;
                inc = 1;
            }

            if (flip_screen() != 0) {
                y = 240 - y;
                x = 240 - x;
                if (fx != 0) {
                    fx = 0;
                } else {
                    fx = 1;
                }
                if (fy != 0) {
                    fy = 0;
                } else {
                    fy = 1;
                }
                mult = 16;
            } else {
                mult = -16;
            }

            while (multi >= 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        sprite - multi * inc,
                        colour,
                        fx, fy,
                        x, y + mult * multi,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
                multi--;
            }
        }
    }

    static void srdarwin_drawsprites(osd_bitmap bitmap, int pri) {
        int offs;

        /* Sprites */
        for (offs = 0; offs < 0x200; offs += 4) {
            int multi, fx, sx, sy, sy2, code, color;

            code = buffered_spriteram.read(offs + 3) + ((buffered_spriteram.read(offs + 1) & 0xe0) << 3);
            sx = (241 - buffered_spriteram.read(offs + 2));
            //if (sx < -7) sx += 256;

            sy = buffered_spriteram.read(offs);
            color = (buffered_spriteram.read(offs + 1) & 0x03) + ((buffered_spriteram.read(offs + 1) & 0x08) >> 1);

            if (pri == 0 && color != 0) {
                continue;
            }
            if (pri == 1 && color == 0) {
                continue;
            }

            fx = buffered_spriteram.read(offs + 1) & 0x04;
            multi = buffered_spriteram.read(offs + 1) & 0x10;

            if (flip_screen() != 0) {
                sy = 240 - sy;
                sx = 240 - sx;
                if (fx != 0) {
                    fx = 0;
                } else {
                    fx = 1;
                }
                sy2 = sy - 16;
            } else {
                sy2 = sy + 16;
            }

            drawgfx(bitmap, Machine.gfx[1],
                    code,
                    color,
                    fx, flip_screen(),
                    sx, sy,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
            if (multi != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        code + 1,
                        color,
                        fx, flip_screen(),
                        sx, sy2,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    }

    /* Draw character tiles, each game has different colour masks */
    static void draw_characters(osd_bitmap bitmap, int mask, int shift) {
        int mx, my, tile, color, offs;

        for (offs = 0x800 - 2; offs >= 0; offs -= 2) {
            tile = videoram.read(offs + 1) + ((videoram.read(offs) & 0xf) << 8);

            if (tile == 0) {
                continue;
            }

            color = (videoram.read(offs) & mask) >> shift;
            mx = (offs / 2) % 32;
            my = (offs / 2) / 32;

            drawgfx(bitmap, Machine.gfx[0],
                    tile, color, 0, 0, 8 * mx, 8 * my,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr cobracom_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x10] << 8) + dec8_pf0_control[0x11]);
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x12] << 8) + dec8_pf0_control[0x13]);
            tilemap_set_scrollx(dec8_pf1_tilemap, 0, (dec8_pf1_control[0x10] << 8) + dec8_pf1_control[0x11]);
            tilemap_set_scrolly(dec8_pf1_tilemap, 0, (dec8_pf1_control[0x12] << 8) + dec8_pf1_control[0x13]);
            flip_screen_w.handler(0, dec8_pf0_control[0] >> 7);

            gfx_mask = 3;
            gfx_bank = 3;
            gfx_base = new UBytePtr(dec8_pf0_data);
            tilemap_update(dec8_pf0_tilemap);

            gfx_bank = 2;
            gfx_base = new UBytePtr(dec8_pf1_data);
            tilemap_update(dec8_pf1_tilemap);
            tilemap_update(dec8_fix_tilemap);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, 0);
            draw_sprites2(bitmap, 1);
            tilemap_draw(bitmap, dec8_pf1_tilemap, 0);
            draw_sprites2(bitmap, 2);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    /**
     * ***************************************************************************
     */
    public static GetTileInfoPtr get_bac0_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int tile, color, offs = tile_index << 1;

            tile = (gfx_base.read(offs) << 8) | gfx_base.read(offs + 1);
            color = tile >> 12;
            if (color > 7 && game_uses_priority != 0) {
                tile_info.u32_priority = 1;
            } else {
                tile_info.u32_priority = 0;
            }

            SET_TILE_INFO(gfx_bank, tile & 0xfff, color & gfx_mask);
        }
    };
    public static GetMemoryOffsetPtr bac0_scan_rows = new GetMemoryOffsetPtr() {
        public int handler(int u32_col, int u32_row, int u32_num_cols, int u32_num_rows) {
            /* logical (col,row) . memory offset */
            return ((u32_col & 0x0f) + ((u32_row & 0x0f) << 4)) + ((u32_col & 0x10) << 5) + ((u32_row & 0x10) << 4);
        }
    };

    public static GetTileInfoPtr get_cobracom_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index << 1;
            int tile = videoram.read(offs + 1) + (videoram.read(offs) << 8);
            int color = (tile & 0xe000) >> 13;

            SET_TILE_INFO(0, tile & 0xfff, color);
        }
    };

    public static VhStartPtr cobracom_vh_start = new VhStartPtr() {
        public int handler() {
            game_uses_priority = 0;
            dec8_pf0_tilemap = tilemap_create(get_bac0_tile_info, bac0_scan_rows, 0, 16, 16, 32, 32);
            dec8_pf1_tilemap = tilemap_create(get_bac0_tile_info, bac0_scan_rows, TILEMAP_TRANSPARENT, 16, 16, 32, 32);
            dec8_fix_tilemap = tilemap_create(get_cobracom_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);

            if (dec8_pf0_tilemap == null || dec8_pf1_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            dec8_pf1_tilemap.transparent_pen = 0;
            dec8_fix_tilemap.transparent_pen = 0;

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr ghostb_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if ((dec8_pf0_control[0] & 0x4) != 0) {
                /* Rowscroll */
                int offs;

                tilemap_set_scroll_rows(dec8_pf0_tilemap, 512);
                for (offs = 0; offs < 512; offs += 2) {
                    tilemap_set_scrollx(dec8_pf0_tilemap, offs / 2, (dec8_pf0_control[0x10] << 8) + dec8_pf0_control[0x11] + (dec8_row.read(offs) << 8) + dec8_row.read(offs + 1));
                }
            } else {
                tilemap_set_scroll_rows(dec8_pf0_tilemap, 1);
                tilemap_set_scrollx(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x10] << 8) + dec8_pf0_control[0x11]);
            }
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x12] << 8) + dec8_pf0_control[0x13]);

            tilemap_update(ALL_TILEMAPS);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, 0);
            draw_sprites1(bitmap, 0);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static GetTileInfoPtr get_ghostb_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index << 1;
            int tile = videoram.read(offs + 1) + (videoram.read(offs) << 8);
            int color = (tile & 0xc00) >> 10;

            SET_TILE_INFO(0, tile & 0x3ff, color);
        }
    };

    public static VhStartPtr ghostb_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_pf0_tilemap = tilemap_create(get_bac0_tile_info, bac0_scan_rows, 0, 16, 16, 32, 32);
            dec8_fix_tilemap = tilemap_create(get_ghostb_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);
            dec8_fix_tilemap.transparent_pen = 0;

            if (dec8_pf0_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            gfx_bank = 2;
            gfx_mask = 0xf;
            gfx_base = new UBytePtr(dec8_pf0_data);

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr oscar_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x10] << 8) + dec8_pf0_control[0x11]);
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, (dec8_pf0_control[0x12] << 8) + dec8_pf0_control[0x13]);
            flip_screen_w.handler(0, dec8_pf0_control[1] >> 7);

            tilemap_update(ALL_TILEMAPS);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 1);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 0);
            draw_sprites2(bitmap, 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 1);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static GetTileInfoPtr get_oscar_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index << 1;
            int tile = videoram.read(offs + 1) + (videoram.read(offs) << 8);
            int color = (tile & 0xf000) >> 14;

            SET_TILE_INFO(0, tile & 0xfff, color);
        }
    };

    public static VhStartPtr oscar_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_pf0_tilemap = tilemap_create(get_bac0_tile_info, bac0_scan_rows, TILEMAP_SPLIT, 16, 16, 32, 32);
            dec8_fix_tilemap = tilemap_create(get_oscar_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);

            if (dec8_pf0_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            dec8_pf0_tilemap.u32_transmask[0] = 0x00ff;
            /* Bottom 8 pens */
            dec8_pf0_tilemap.u32_transmask[1] = 0xff00;
            /* Top 8 pens */
            game_uses_priority = 1;
            gfx_bank = 2;
            gfx_mask = 0x7;
            gfx_base = new UBytePtr(dec8_pf0_data);

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr lastmiss_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, ((scroll2[0] << 8) + scroll2[1]));
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, ((scroll2[2] << 8) + scroll2[3]));
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            memset(palette_used_colors, 256, PALETTE_COLOR_USED, 256);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, 0);
            draw_sprites1(bitmap, 0);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static VhUpdatePtr shackled_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, ((scroll2[0] << 8) + scroll2[1]));
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, ((scroll2[2] << 8) + scroll2[3]));
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            memset(palette_used_colors, 256, PALETTE_COLOR_USED, 256);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 1);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 0);
            draw_sprites1(bitmap, 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 1);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };
    public static GetMemoryOffsetPtr lastmiss_scan_rows = new GetMemoryOffsetPtr() {
        public int handler(int u32_col, int u32_row, int u32_num_cols, int u32_num_rows) {
            /* logical (col,row) . memory offset */
            return ((u32_col & 0x0f) + ((u32_row & 0x0f) << 4)) + ((u32_col & 0x10) << 4) + ((u32_row & 0x10) << 5);
        }
    };

    public static GetTileInfoPtr get_lastmiss_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index * 2;
            int tile = dec8_pf0_data.read(offs + 1) + (dec8_pf0_data.read(offs) << 8);
            int color = tile >> 12;

            if (color > 7 && game_uses_priority != 0) {
                tile_info.u32_priority = 1;
            } else {
                tile_info.u32_priority = 0;
            }

            SET_TILE_INFO(2, tile & 0xfff, color);
        }
    };

    public static GetTileInfoPtr get_lastmiss_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index << 1;
            int tile = videoram.read(offs + 1) + (videoram.read(offs) << 8);
            int color = (tile & 0xc000) >> 14;

            SET_TILE_INFO(0, tile & 0xfff, color);
        }
    };

    public static VhStartPtr lastmiss_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_pf0_tilemap = tilemap_create(get_lastmiss_tile_info, lastmiss_scan_rows, 0, 16, 16, 32, 32);
            dec8_fix_tilemap = tilemap_create(get_lastmiss_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);

            if (dec8_pf0_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            game_uses_priority = 0;

            return 0;
        }
    };

    public static VhStartPtr shackled_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_pf0_tilemap = tilemap_create(get_lastmiss_tile_info, lastmiss_scan_rows, TILEMAP_SPLIT, 16, 16, 32, 32);
            dec8_fix_tilemap = tilemap_create(get_lastmiss_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);

            if (dec8_pf0_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            dec8_pf0_tilemap.u32_transmask[0] = 0x000f;
            /* Bottom 12 pens */
            dec8_pf0_tilemap.u32_transmask[1] = 0xfff0;
            /* Top 4 pens */
            game_uses_priority = 1;

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr srdarwin_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, (scroll2[0] << 8) + scroll2[1]);
            tilemap_update(ALL_TILEMAPS);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 1);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK | 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 1);
            srdarwin_drawsprites(bitmap, 0);
            /* Priority may not be right on later levels */
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT | 0);
            srdarwin_drawsprites(bitmap, 1);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static GetTileInfoPtr get_srdarwin_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int tile = videoram.read(tile_index);
            int color = 0;
            /* ? */

            if (color > 1) {
                tile_info.u32_priority = 1;
            } else {
                tile_info.u32_priority = 0;
            }

            SET_TILE_INFO(0, tile, color);
        }
    };

    public static GetTileInfoPtr get_srdarwin_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int tile = dec8_pf0_data.read(2 * tile_index + 1) + (dec8_pf0_data.read(2 * tile_index) << 8);
            int color = tile >> 12;
            int bank;

            tile = tile & 0xfff;
            bank = (tile / 0x100) + 2;

            SET_TILE_INFO(bank, tile, color);
        }
    };

    public static VhStartPtr srdarwin_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_pf0_tilemap = tilemap_create(get_srdarwin_tile_info, tilemap_scan_rows, TILEMAP_SPLIT, 16, 16, 32, 16);
            dec8_fix_tilemap = tilemap_create(get_srdarwin_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);

            if (dec8_pf0_tilemap == null || dec8_fix_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            dec8_pf0_tilemap.u32_transmask[0] = 0x00ff;
            /* Bottom 8 pens */
            dec8_pf0_tilemap.u32_transmask[1] = 0xff00;
            /* Top 8 pens */

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhUpdatePtr gondo_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, ((scroll2[0] << 8) + scroll2[1]));
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, ((scroll2[2] << 8) + scroll2[3]));
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            memset(palette_used_colors, 256, PALETTE_COLOR_USED, 256);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_BACK);
            draw_sprites1(bitmap, 2);
            tilemap_draw(bitmap, dec8_pf0_tilemap, TILEMAP_FRONT);
            draw_sprites1(bitmap, 1);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static VhUpdatePtr garyoret_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(dec8_pf0_tilemap, 0, ((scroll2[0] << 8) + scroll2[1]));
            tilemap_set_scrolly(dec8_pf0_tilemap, 0, ((scroll2[2] << 8) + scroll2[3]));
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            memset(palette_used_colors, 256, PALETTE_COLOR_USED, 256);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, dec8_pf0_tilemap, 0);
            draw_sprites1(bitmap, 0);
            tilemap_draw(bitmap, dec8_pf0_tilemap, 1);
            tilemap_draw(bitmap, dec8_fix_tilemap, 0);
        }
    };

    public static GetTileInfoPtr get_gondo_fix_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index * 2;
            int tile = videoram.read(offs + 1) + (videoram.read(offs) << 8);
            int color = (tile & 0x7000) >> 12;

            SET_TILE_INFO(0, tile & 0xfff, color);
        }
    };

    public static GetTileInfoPtr get_gondo_tile_info = new GetTileInfoPtr() {
        public void handler(int tile_index) {
            int offs = tile_index * 2;
            int tile = dec8_pf0_data.read(offs + 1) + (dec8_pf0_data.read(offs) << 8);
            int color = tile >> 12;

            if (color > 7 && game_uses_priority != 0) {
                tile_info.u32_priority = 1;
            } else {
                tile_info.u32_priority = 0;
            }

            SET_TILE_INFO(2, tile & 0xfff, color);
        }
    };

    public static VhStartPtr gondo_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_fix_tilemap = tilemap_create(get_gondo_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);
            dec8_pf0_tilemap = tilemap_create(get_gondo_tile_info, tilemap_scan_rows, TILEMAP_SPLIT, 16, 16, 32, 32);

            if (dec8_fix_tilemap == null || dec8_pf0_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            dec8_pf0_tilemap.u32_transmask[0] = 0x00ff;
            /* Bottom 8 pens */
            dec8_pf0_tilemap.u32_transmask[1] = 0xff00;
            /* Top 8 pens */
            game_uses_priority = 0;

            return 0;
        }
    };

    public static VhStartPtr garyoret_vh_start = new VhStartPtr() {
        public int handler() {
            dec8_fix_tilemap = tilemap_create(get_gondo_fix_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 8, 8, 32, 32);
            dec8_pf0_tilemap = tilemap_create(get_gondo_tile_info, tilemap_scan_rows, TILEMAP_SPLIT, 16, 16, 32, 32);

            if (dec8_fix_tilemap == null || dec8_pf0_tilemap == null) {
                return 1;
            }

            dec8_fix_tilemap.transparent_pen = 0;
            game_uses_priority = 1;

            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
}
