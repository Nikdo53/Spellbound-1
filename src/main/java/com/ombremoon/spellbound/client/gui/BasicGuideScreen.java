package com.ombremoon.spellbound.client.gui;

import com.lowdragmc.lowdraglib2.test.TestJava;
import com.mojang.datafixers.util.Pair;
import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookManager;
import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookPage;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BasicGuideScreen extends GuideBookScreen {
    private BookMark[] bookMarks;

    public BasicGuideScreen(Component title) {
        super(title, CommonClass.customLocation("studies_in_the_arcane"), CommonClass.customLocation("textures/gui/books/studies_in_the_arcane.png"));
    }

    @Override
    protected void init() {
        super.init();
        bookMarks = new BookMark[] {
                new BookMark(56, 244, 79,280, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_cover_page"))),
                new BookMark(105, 243, 127,277, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_ruin_cover"))),
                new BookMark( 150, 244, 173, 278, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_transfig_cover"))),
                new BookMark(231, 243, 254, 278, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_summon_cover"))),
                new BookMark(283, 244, 306, 278, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_deception_cover"))),
                new BookMark(330, 244, 353, 280, GuideBookManager.getBasicBookmark(CommonClass.customLocation("basic_divine_cover")))
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (BookMark bookMark : bookMarks) {
            if (bookMark.isHovering(mouseX, mouseY, this.leftPos, this.topPos)) {
                currentPage = bookMark.page;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public record BookMark(int left, int top, int right, int bottom, int page) {
        public boolean isHovering(double mouseX, double mouseY, int bookLeft, int bookTop) {
            return mouseX > left + bookLeft
                    && mouseX < right + bookLeft
                    && mouseY > top + bookTop
                    && mouseY < bottom + bookTop;
        }
    }
}
