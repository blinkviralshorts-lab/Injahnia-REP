package com.example.ui.screens.chat

enum class UnitDisplayMode(val label: String) {
    DUAL("Dual (mm & in)"),
    METRIC("Metric (mm/Nm)"),
    IMPERIAL("Imperial (in/ft-lb)")
}

data class SpecTableRow(
    val cells: List<String>
)

data class SpecTableData(
    val title: String? = null,
    val headers: List<String>,
    val rows: List<SpecTableRow>
)

data class ToleranceCardItem(
    val parameter: String,
    val rangeMm: String,
    val rangeInch: String? = null,
    val targetMm: String? = null,
    val maxLimitMm: String? = null,
    val unit: String = "mm",
    val category: String? = null,
    val statusBadge: String? = null
)

data class TorqueStepItem(
    val stepIndex: Int,
    val stepTitle: String,
    val torqueMetric: String,
    val torqueImperial: String? = null,
    val angleDegrees: String? = null,
    val notes: String? = null
)

data class TorqueSequenceData(
    val fastenerTitle: String,
    val boltType: String? = null,
    val lubricant: String? = null,
    val steps: List<TorqueStepItem>
)

sealed class ParsedChatBlock {
    data class TextBlock(val content: String) : ParsedChatBlock()
    data class TableBlock(val table: SpecTableData) : ParsedChatBlock()
    data class ToleranceBlock(val title: String, val items: List<ToleranceCardItem>) : ParsedChatBlock()
    data class TorqueBlock(val sequence: TorqueSequenceData) : ParsedChatBlock()
    data class CalloutBlock(val title: String, val message: String, val isWarning: Boolean) : ParsedChatBlock()
}

object SpecContentParser {

    /**
     * Parses raw markdown / text from the AI specialist or engine database
     * into a list of structured blocks (tables, tolerance groups, torque sequences, text).
     */
    fun parse(rawText: String): List<ParsedChatBlock> {
        val blocks = mutableListOf<ParsedChatBlock>()
        val lines = rawText.lines()
        var i = 0

        val currentTextAccumulator = mutableListOf<String>()

        fun flushText() {
            val text = currentTextAccumulator.joinToString("\n").trim()
            if (text.isNotBlank()) {
                blocks.add(ParsedChatBlock.TextBlock(text))
            }
            currentTextAccumulator.clear()
        }

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // 1. Detect Markdown Table
            if (isTableLine(trimmed)) {
                flushText()
                val tableLines = mutableListOf<String>()
                while (i < lines.size && isTableLine(lines[i].trim())) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                val tableData = parseMarkdownTable(tableLines)
                if (tableData != null && tableData.rows.isNotEmpty()) {
                    blocks.add(ParsedChatBlock.TableBlock(tableData))
                } else {
                    // Fallback to text if table parsing was invalid
                    tableLines.forEach { currentTextAccumulator.add(it) }
                }
                continue
            }

            // 2. Detect Torque Sequence
            if (trimmed.startsWith("### TORQUE", ignoreCase = true) ||
                trimmed.startsWith("TORQUE SPECIFICATION", ignoreCase = true) ||
                (trimmed.contains("Torque", ignoreCase = true) && trimmed.contains("Sequence", ignoreCase = true) && trimmed.endsWith(":"))
            ) {
                flushText()
                val title = trimmed.replace("#", "").trim().removeSuffix(":")
                i++
                val steps = mutableListOf<TorqueStepItem>()
                var boltType: String? = null
                var lubricant: String? = null

                while (i < lines.size && lines[i].isNotBlank()) {
                    val stepLine = lines[i].trim()
                    if (stepLine.startsWith("-") || stepLine.startsWith("•") || stepLine.startsWith("Step", ignoreCase = true) || stepLine.matches(Regex("^[0-9]+\\..*"))) {
                        val parsedStep = parseTorqueStep(stepLine, steps.size + 1)
                        if (parsedStep != null) {
                            steps.add(parsedStep)
                        } else if (stepLine.contains("bolt", ignoreCase = true)) {
                            boltType = stepLine.removePrefix("-").removePrefix("•").trim()
                        } else if (stepLine.contains("lube", ignoreCase = true) || stepLine.contains("oil", ignoreCase = true)) {
                            lubricant = stepLine.removePrefix("-").removePrefix("•").trim()
                        }
                    } else if (stepLine.contains("lube", ignoreCase = true) || stepLine.contains("oil", ignoreCase = true)) {
                        lubricant = stepLine
                    }
                    i++
                }

                if (steps.isNotEmpty()) {
                    blocks.add(
                        ParsedChatBlock.TorqueBlock(
                            TorqueSequenceData(
                                fastenerTitle = title,
                                boltType = boltType,
                                lubricant = lubricant,
                                steps = steps
                            )
                        )
                    )
                }
                continue
            }

            // 3. Detect Warning Callouts
            if (trimmed.startsWith("[CRITICAL]") || trimmed.startsWith("⚠️") || trimmed.startsWith("WARNING:")) {
                flushText()
                val clean = trimmed.removePrefix("[CRITICAL]").removePrefix("⚠️").removePrefix("WARNING:").trim()
                blocks.add(
                    ParsedChatBlock.CalloutBlock(
                        title = "CRITICAL MACHINING WARNING",
                        message = clean,
                        isWarning = true
                    )
                )
                i++
                continue
            }

            // 4. Detect consecutive tolerance/spec lines
            if (isToleranceLine(trimmed)) {
                val candidateToleranceLines = mutableListOf<String>()
                candidateToleranceLines.add(trimmed)
                var j = i + 1
                while (j < lines.size && (isToleranceLine(lines[j].trim()) || lines[j].trim().isEmpty())) {
                    if (lines[j].trim().isNotEmpty()) {
                        candidateToleranceLines.add(lines[j].trim())
                    }
                    j++
                }

                // If at least 2 tolerance spec lines appear together, group them
                if (candidateToleranceLines.size >= 2) {
                    flushText()
                    val toleranceItems = candidateToleranceLines.mapNotNull { parseToleranceItem(it) }
                    if (toleranceItems.isNotEmpty()) {
                        blocks.add(
                            ParsedChatBlock.ToleranceBlock(
                                title = "ENGINE CLEARANCES & MACHINING TOLERANCES",
                                items = toleranceItems
                            )
                        )
                        i = j
                        continue
                    }
                }
            }

            // Regular text line
            currentTextAccumulator.add(line)
            i++
        }

        flushText()
        return blocks
    }

    private fun isTableLine(line: String): Boolean {
        return line.startsWith("|") && line.endsWith("|") && line.count { it == '|' } >= 3
    }

    private fun parseMarkdownTable(lines: List<String>): SpecTableData? {
        if (lines.size < 2) return null
        val rawHeaders = lines[0].split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (rawHeaders.isEmpty()) return null

        // Check if line 1 is separator (e.g. |---|---|)
        val startIndex = if (lines.size > 1 && lines[1].contains("---")) 2 else 1

        val rows = mutableListOf<SpecTableRow>()
        for (k in startIndex until lines.size) {
            val cells = lines[k].split("|").map { it.trim() }.drop(1).dropLast(1)
            if (cells.isNotEmpty()) {
                rows.add(SpecTableRow(cells))
            }
        }

        return SpecTableData(
            headers = rawHeaders,
            rows = rows
        )
    }

    private fun isToleranceLine(line: String): Boolean {
        val clean = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
        if (!clean.contains(":")) return false
        val key = clean.substringBefore(":").lowercase()
        val value = clean.substringAfter(":").trim()

        val hasSpecKeyword = key.contains("clearance") || key.contains("tolerance") ||
                key.contains("bore") || key.contains("gap") || key.contains("journal") ||
                key.contains("deck") || key.contains("finish") || key.contains("torque") ||
                key.contains("diameter") || key.contains("piston") || key.contains("bearing") ||
                key.contains("lash") || key.contains("protrusion") || key.contains("recession") ||
                key.contains("preload") || key.contains("thrust") || key.contains("stroke")

        val hasNumericSpec = value.any { it.isDigit() } && (
                value.contains("mm") || value.contains("in") || value.contains("\"") ||
                        value.contains("µ") || value.contains("Nm") || value.contains("ft-lb") ||
                        value.contains("-") || value.contains("–") || value.contains("to") || value.contains("°")
                )

        return hasSpecKeyword && hasNumericSpec
    }

    private fun parseToleranceItem(line: String): ToleranceCardItem? {
        val clean = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
        val parts = clean.split(":", limit = 2)
        if (parts.size != 2) return null

        val paramName = parts[0].replace("**", "").trim()
        val rawValue = parts[1].replace("**", "").trim()

        // Extract Metric Range vs Imperial Range if present (e.g. 0.026 - 0.045 mm (0.0010" - 0.0018"))
        var rangeMm = rawValue
        var rangeInch: String? = null
        var maxLimit: String? = null
        var statusBadge: String? = null

        if (rawValue.contains("(") && rawValue.contains(")")) {
            val beforeParen = rawValue.substringBefore("(").trim()
            val insideParen = rawValue.substringAfter("(").substringBefore(")").trim()

            if (insideParen.contains("\"") || insideParen.contains("in") || insideParen.contains("ft-lb")) {
                rangeMm = beforeParen
                rangeInch = insideParen
            } else if (insideParen.contains("Max", ignoreCase = true) || insideParen.contains("Limit", ignoreCase = true)) {
                rangeMm = beforeParen
                maxLimit = insideParen
            } else {
                rangeMm = beforeParen
                statusBadge = insideParen
            }
        }

        if (rawValue.contains("Max:", ignoreCase = true)) {
            maxLimit = rawValue.substringAfter("Max:").trim().take(20)
        }

        if (paramName.contains("piston", ignoreCase = true)) {
            statusBadge = statusBadge ?: "Piston Skirt"
        } else if (paramName.contains("bearing", ignoreCase = true)) {
            statusBadge = statusBadge ?: "Oil Clearance"
        } else if (paramName.contains("ring", ignoreCase = true)) {
            statusBadge = statusBadge ?: "End Gap"
        } else if (paramName.contains("deck", ignoreCase = true) || paramName.contains("finish", ignoreCase = true)) {
            statusBadge = statusBadge ?: "Surface Ra"
        }

        return ToleranceCardItem(
            parameter = paramName,
            rangeMm = rangeMm,
            rangeInch = rangeInch,
            maxLimitMm = maxLimit,
            statusBadge = statusBadge
        )
    }

    private fun parseTorqueStep(line: String, defaultIndex: Int): TorqueStepItem? {
        val clean = line.removePrefix("-").removePrefix("•").trim()
        val index = if (clean.startsWith("Step", ignoreCase = true)) {
            clean.substringAfter("Step").trim().takeWhile { it.isDigit() }.toIntOrNull() ?: defaultIndex
        } else {
            defaultIndex
        }

        val stepText = clean.replace(Regex("^Step\\s*\\d+[:\\-]?\\s*", RegexOption.IGNORE_CASE), "")
        val angleMatch = Regex("(\\+?\\d+°(\\s*TTY)?)").find(stepText)
        val angle = angleMatch?.value

        var metric = stepText
        var imperial: String? = null
        var notes: String? = null

        if (stepText.contains("(") && stepText.contains(")")) {
            imperial = stepText.substringAfter("(").substringBefore(")").trim()
            metric = stepText.substringBefore("(").trim()
        }

        if (stepText.contains(" - ")) {
            notes = stepText.substringAfter(" - ").trim()
            metric = metric.substringBefore(" - ").trim()
        }

        return TorqueStepItem(
            stepIndex = index,
            stepTitle = "Step $index",
            torqueMetric = metric,
            torqueImperial = imperial,
            angleDegrees = angle,
            notes = notes
        )
    }
}
