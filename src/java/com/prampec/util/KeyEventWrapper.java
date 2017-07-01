/*
 * File: KeyEventWrapper.java
 * Description:
 *    RivetCam is an open source photographic software, where you
 *    can capture still images, potentially use to create stop-motion videos.
 *    Documentation: https://doc.csipa.hu/?page_id=415
 *
 * Author: Balazs Kelemen
 * Contact: prampec+rivetcam@gmail.com
 * Copyright: 2017 Balazs Kelemen
 * Copying permission statement:
 *     This file is part of RivetCam.
 *     RivetCam is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prampec.util;

import java.awt.event.KeyEvent;

/**
 * Wraps KeyEvent virtual keys into an enum.
 * Created by kelemenb on 6/24/17.
 */
public enum KeyEventWrapper {
    VK_ENTER(KeyEvent.VK_ENTER),
    VK_BACK_SPACE(KeyEvent.VK_BACK_SPACE),
    VK_TAB(KeyEvent.VK_TAB),
    VK_CANCEL(KeyEvent.VK_CANCEL),
    VK_CLEAR(KeyEvent.VK_CLEAR),
    VK_SHIFT(KeyEvent.VK_SHIFT),
    VK_CONTROL(KeyEvent.VK_CONTROL),
    VK_ALT(KeyEvent.VK_ALT),
    VK_PAUSE(KeyEvent.VK_PAUSE),
    VK_CAPS_LOCK(KeyEvent.VK_CAPS_LOCK),
    VK_ESCAPE(KeyEvent.VK_ESCAPE),
    VK_SPACE(KeyEvent.VK_SPACE),
    VK_PAGE_UP(KeyEvent.VK_PAGE_UP),
    VK_PAGE_DOWN(KeyEvent.VK_PAGE_DOWN),
    VK_END(KeyEvent.VK_END),
    VK_HOME(KeyEvent.VK_HOME),
    VK_LEFT(KeyEvent.VK_LEFT),
    VK_UP(KeyEvent.VK_UP),
    VK_RIGHT(KeyEvent.VK_RIGHT),
    VK_DOWN(KeyEvent.VK_DOWN),
    VK_COMMA(KeyEvent.VK_COMMA),
    VK_MINUS(KeyEvent.VK_MINUS),
    VK_PERIOD(KeyEvent.VK_PERIOD),
    VK_SLASH(KeyEvent.VK_SLASH),
    VK_0(KeyEvent.VK_0),
    VK_1(KeyEvent.VK_1),
    VK_2(KeyEvent.VK_2),
    VK_3(KeyEvent.VK_3),
    VK_4(KeyEvent.VK_4),
    VK_5(KeyEvent.VK_5),
    VK_6(KeyEvent.VK_6),
    VK_7(KeyEvent.VK_7),
    VK_8(KeyEvent.VK_8),
    VK_9(KeyEvent.VK_9),
    VK_SEMICOLON(KeyEvent.VK_SEMICOLON),
    VK_EQUALS(KeyEvent.VK_EQUALS),
    VK_A(KeyEvent.VK_A),
    VK_B(KeyEvent.VK_B),
    VK_C(KeyEvent.VK_C),
    VK_D(KeyEvent.VK_D),
    VK_E(KeyEvent.VK_E),
    VK_F(KeyEvent.VK_F),
    VK_G(KeyEvent.VK_G),
    VK_H(KeyEvent.VK_H),
    VK_I(KeyEvent.VK_I),
    VK_J(KeyEvent.VK_J),
    VK_K(KeyEvent.VK_K),
    VK_L(KeyEvent.VK_L),
    VK_M(KeyEvent.VK_M),
    VK_N(KeyEvent.VK_N),
    VK_O(KeyEvent.VK_O),
    VK_P(KeyEvent.VK_P),
    VK_Q(KeyEvent.VK_Q),
    VK_R(KeyEvent.VK_R),
    VK_S(KeyEvent.VK_S),
    VK_T(KeyEvent.VK_T),
    VK_U(KeyEvent.VK_U),
    VK_V(KeyEvent.VK_V),
    VK_W(KeyEvent.VK_W),
    VK_X(KeyEvent.VK_X),
    VK_Y(KeyEvent.VK_Y),
    VK_Z(KeyEvent.VK_Z),
    VK_OPEN_BRACKET(KeyEvent.VK_OPEN_BRACKET),
    VK_BACK_SLASH(KeyEvent.VK_BACK_SLASH),
    VK_CLOSE_BRACKET(KeyEvent.VK_CLOSE_BRACKET),
    VK_NUMPAD0(KeyEvent.VK_NUMPAD0),
    VK_NUMPAD1(KeyEvent.VK_NUMPAD1),
    VK_NUMPAD2(KeyEvent.VK_NUMPAD2),
    VK_NUMPAD3(KeyEvent.VK_NUMPAD3),
    VK_NUMPAD4(KeyEvent.VK_NUMPAD4),
    VK_NUMPAD5(KeyEvent.VK_NUMPAD5),
    VK_NUMPAD6(KeyEvent.VK_NUMPAD6),
    VK_NUMPAD7(KeyEvent.VK_NUMPAD7),
    VK_NUMPAD8(KeyEvent.VK_NUMPAD8),
    VK_NUMPAD9(KeyEvent.VK_NUMPAD9),
    VK_MULTIPLY(KeyEvent.VK_MULTIPLY),
    VK_ADD(KeyEvent.VK_ADD),
    VK_SEPARATER(KeyEvent.VK_SEPARATER),
    VK_SEPARATOR(KeyEvent.VK_SEPARATOR),
    VK_SUBTRACT(KeyEvent.VK_SUBTRACT),
    VK_DECIMAL(KeyEvent.VK_DECIMAL),
    VK_DIVIDE(KeyEvent.VK_DIVIDE),
    VK_DELETE(KeyEvent.VK_DELETE),
    VK_NUM_LOCK(KeyEvent.VK_NUM_LOCK),
    VK_SCROLL_LOCK(KeyEvent.VK_SCROLL_LOCK),
    VK_F1(KeyEvent.VK_F1),
    VK_F2(KeyEvent.VK_F2),
    VK_F3(KeyEvent.VK_F3),
    VK_F4(KeyEvent.VK_F4),
    VK_F5(KeyEvent.VK_F5),
    VK_F6(KeyEvent.VK_F6),
    VK_F7(KeyEvent.VK_F7),
    VK_F8(KeyEvent.VK_F8),
    VK_F9(KeyEvent.VK_F9),
    VK_F10(KeyEvent.VK_F10),
    VK_F11(KeyEvent.VK_F11),
    VK_F12(KeyEvent.VK_F12),
    VK_F13(KeyEvent.VK_F13),
    VK_F14(KeyEvent.VK_F14),
    VK_F15(KeyEvent.VK_F15),
    VK_F16(KeyEvent.VK_F16),
    VK_F17(KeyEvent.VK_F17),
    VK_F18(KeyEvent.VK_F18),
    VK_F19(KeyEvent.VK_F19),
    VK_F20(KeyEvent.VK_F20),
    VK_F21(KeyEvent.VK_F21),
    VK_F22(KeyEvent.VK_F22),
    VK_F23(KeyEvent.VK_F23),
    VK_F24(KeyEvent.VK_F24),
    VK_PRINTSCREEN(KeyEvent.VK_PRINTSCREEN),
    VK_INSERT(KeyEvent.VK_INSERT),
    VK_HELP(KeyEvent.VK_HELP),
    VK_META(KeyEvent.VK_META),
    VK_BACK_QUOTE(KeyEvent.VK_BACK_QUOTE),
    VK_QUOTE(KeyEvent.VK_QUOTE),
    VK_KP_UP(KeyEvent.VK_KP_UP),
    VK_KP_DOWN(KeyEvent.VK_KP_DOWN),
    VK_KP_LEFT(KeyEvent.VK_KP_LEFT),
    VK_KP_RIGHT(KeyEvent.VK_KP_RIGHT),
    VK_DEAD_GRAVE(KeyEvent.VK_DEAD_GRAVE),
    VK_DEAD_ACUTE(KeyEvent.VK_DEAD_ACUTE),
    VK_DEAD_CIRCUMFLEX(KeyEvent.VK_DEAD_CIRCUMFLEX),
    VK_DEAD_TILDE(KeyEvent.VK_DEAD_TILDE),
    VK_DEAD_MACRON(KeyEvent.VK_DEAD_MACRON),
    VK_DEAD_BREVE(KeyEvent.VK_DEAD_BREVE),
    VK_DEAD_ABOVEDOT(KeyEvent.VK_DEAD_ABOVEDOT),
    VK_DEAD_DIAERESIS(KeyEvent.VK_DEAD_DIAERESIS),
    VK_DEAD_ABOVERING(KeyEvent.VK_DEAD_ABOVERING),
    VK_DEAD_DOUBLEACUTE(KeyEvent.VK_DEAD_DOUBLEACUTE),
    VK_DEAD_CARON(KeyEvent.VK_DEAD_CARON),
    VK_DEAD_CEDILLA(KeyEvent.VK_DEAD_CEDILLA),
    VK_DEAD_OGONEK(KeyEvent.VK_DEAD_OGONEK),
    VK_DEAD_IOTA(KeyEvent.VK_DEAD_IOTA),
    VK_DEAD_VOICED_SOUND(KeyEvent.VK_DEAD_VOICED_SOUND),
    VK_DEAD_SEMIVOICED_SOUND(KeyEvent.VK_DEAD_SEMIVOICED_SOUND),
    VK_AMPERSAND(KeyEvent.VK_AMPERSAND),
    VK_ASTERISK(KeyEvent.VK_ASTERISK),
    VK_QUOTEDBL(KeyEvent.VK_QUOTEDBL),
    VK_LESS(KeyEvent.VK_LESS),
    VK_GREATER(KeyEvent.VK_GREATER),
    VK_BRACELEFT(KeyEvent.VK_BRACELEFT),
    VK_BRACERIGHT(KeyEvent.VK_BRACERIGHT),
    VK_AT(KeyEvent.VK_AT),
    VK_COLON(KeyEvent.VK_COLON),
    VK_CIRCUMFLEX(KeyEvent.VK_CIRCUMFLEX),
    VK_DOLLAR(KeyEvent.VK_DOLLAR),
    VK_EURO_SIGN(KeyEvent.VK_EURO_SIGN),
    VK_EXCLAMATION_MARK(KeyEvent.VK_EXCLAMATION_MARK),
    VK_INVERTED_EXCLAMATION_MARK(KeyEvent.VK_INVERTED_EXCLAMATION_MARK),
    VK_LEFT_PARENTHESIS(KeyEvent.VK_LEFT_PARENTHESIS),
    VK_NUMBER_SIGN(KeyEvent.VK_NUMBER_SIGN),
    VK_PLUS(KeyEvent.VK_PLUS),
    VK_RIGHT_PARENTHESIS(KeyEvent.VK_RIGHT_PARENTHESIS),
    VK_UNDERSCORE(KeyEvent.VK_UNDERSCORE),
    VK_WINDOWS(KeyEvent.VK_WINDOWS),
    VK_CONTEXT_MENU(KeyEvent.VK_CONTEXT_MENU),
    VK_FINAL(KeyEvent.VK_FINAL),
    VK_CONVERT(KeyEvent.VK_CONVERT),
    VK_NONCONVERT(KeyEvent.VK_NONCONVERT),
    VK_ACCEPT(KeyEvent.VK_ACCEPT),
    VK_MODECHANGE(KeyEvent.VK_MODECHANGE),
    VK_KANA(KeyEvent.VK_KANA),
    VK_KANJI(KeyEvent.VK_KANJI),
    VK_ALPHANUMERIC(KeyEvent.VK_ALPHANUMERIC),
    VK_KATAKANA(KeyEvent.VK_KATAKANA),
    VK_HIRAGANA(KeyEvent.VK_HIRAGANA),
    VK_FULL_WIDTH(KeyEvent.VK_FULL_WIDTH),
    VK_HALF_WIDTH(KeyEvent.VK_HALF_WIDTH),
    VK_ROMAN_CHARACTERS(KeyEvent.VK_ROMAN_CHARACTERS),
    VK_ALL_CANDIDATES(KeyEvent.VK_ALL_CANDIDATES),
    VK_PREVIOUS_CANDIDATE(KeyEvent.VK_PREVIOUS_CANDIDATE),
    VK_CODE_INPUT(KeyEvent.VK_CODE_INPUT),
    VK_JAPANESE_KATAKANA(KeyEvent.VK_JAPANESE_KATAKANA),
    VK_JAPANESE_HIRAGANA(KeyEvent.VK_JAPANESE_HIRAGANA),
    VK_JAPANESE_ROMAN(KeyEvent.VK_JAPANESE_ROMAN),
    VK_KANA_LOCK(KeyEvent.VK_KANA_LOCK),
    VK_INPUT_METHOD_ON_OFF(KeyEvent.VK_INPUT_METHOD_ON_OFF),
    VK_CUT(KeyEvent.VK_CUT),
    VK_COPY(KeyEvent.VK_COPY),
    VK_PASTE(KeyEvent.VK_PASTE),
    VK_UNDO(KeyEvent.VK_UNDO),
    VK_AGAIN(KeyEvent.VK_AGAIN),
    VK_FIND(KeyEvent.VK_FIND),
    VK_PROPS(KeyEvent.VK_PROPS),
    VK_STOP(KeyEvent.VK_STOP),
    VK_COMPOSE(KeyEvent.VK_COMPOSE),
    VK_ALT_GRAPH(KeyEvent.VK_ALT_GRAPH),
    VK_BEGIN(KeyEvent.VK_BEGIN),
    VK_UNDEFINED(KeyEvent.VK_UNDEFINED),;

    private int keyEvent;

    KeyEventWrapper(int keyEvent) {
        this.keyEvent = keyEvent;
    }

    public int getKeyEvent() {
        return keyEvent;
    }


    @Override
    public String toString() {
        return name().substring(3);
    }
}
