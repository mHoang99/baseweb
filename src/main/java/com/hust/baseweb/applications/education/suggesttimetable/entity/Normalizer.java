package com.hust.baseweb.applications.education.suggesttimetable.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.util.Optional;

public class Normalizer {

    private Normalizer() {
    }

    private static Optional<String> formatCell(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String value = StringUtils.deleteWhitespace(formatter.formatCellValue(cell));

        if (StringUtils.equalsIgnoreCase("NULL", value) || StringUtils.equalsIgnoreCase("", value)) {
            return Optional.empty();
        }

        return Optional.ofNullable(value);
    }

    public static Integer normalizeInt(Cell cell) {
        String value = formatCell(cell).orElse(null);
        return NumberUtils.toInt(value);
    }

    public static String normalizeStr(Cell cell) {
        return formatCell(cell).orElse(null);
    }
}
