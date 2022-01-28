package command.implementations

import command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.DeviationRenderer
import org.jfree.chart.renderer.xy.XYAreaRenderer
import org.jfree.chart.renderer.xy.XYDifferenceRenderer
import org.jfree.chart.renderer.xy.XYSplineRenderer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.io.File
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sqrt

private val cmd = CommandData("accel", "will calculate a acceleration diagram and display it")
    .addOption(OptionType.NUMBER, "acceleration", "The acceleration of the movement in mm/s²", true)
    .addOption(OptionType.NUMBER, "desired-speed", "The speed it should reach in mm/s", true)
    .addOption(OptionType.NUMBER, "mass", "mass of the moving gantry im g", true)
    .addOption(OptionType.NUMBER, "movement-length", "The size of the movement in mm", true)

class AccelCommand: Command("accel", cmd, false) {
    override fun execute(member: Member, event: SlashCommandEvent) {
        val acceleration = event.getOption("acceleration")!!.asDouble
        var speed = event.getOption("desired-speed")!!.asDouble
        val givenSpeed = speed
        val mass = event.getOption("mass")!!.asDouble
        val length = event.getOption("movement-length")!!.asDouble
        val accelSet = XYSeries("Acceleration")
        val speedSet = XYSeries("Speed")
        val stepl = .5
        var time = 0.0;

        val timeToFullSpeed = speed/acceleration
        val distanceToFullSpeed = (acceleration*timeToFullSpeed*timeToFullSpeed)/2


        if (length/2 <= distanceToFullSpeed) {
            time = sqrt(length/acceleration)
            speed = acceleration*time
        }

        var minDistance = length
        var maxDistance = length

        for (i in generateSequence(0.0) { it + stepl }.takeWhile { it <= length / 2}) {
            time = sqrt(2*i / acceleration)
            speed = time*acceleration

            if (speed >= givenSpeed) {
                if (i < minDistance) minDistance = i
                speedSet.add(i, givenSpeed)
            }

            accelSet.add(i, speed)
        }

        for (i in generateSequence(length/2) { it + stepl }.takeWhile { it <= length}) {
            val mirroredDistance = length - i
            time = sqrt(2* mirroredDistance/ acceleration)
            speed = time*acceleration

            if (speed >= givenSpeed) {
                if (i < maxDistance) maxDistance = i.toDouble()
                speedSet.add(i, givenSpeed)
            }

            accelSet.add(i, speed)
        }


        accelSet.add(length, 0)

        val renderer = XYSplineRenderer()
        val x = NumberAxis("Distance")
        val y = NumberAxis("Speed")
        val data = XYSeriesCollection(accelSet)
        data.addSeries(speedSet)
        val plot = XYPlot(data, x, y, renderer)
        val chart = JFreeChart(plot)

        val temp = File("temp.jpg")
        ChartUtilities.saveChartAsPNG(temp, chart, 1920, 1080)

        val embed = EmbedBuilder()
        embed.setTitle("Results")
        embed.setColor(Color.GREEN)

        embed.addField("Maximum force", "${acceleration/1000 * mass/1000}N", true)
        embed.addField("Acceleration", "${acceleration}mm/s²", true)
        embed.addField("Desired speed", "${givenSpeed}mm/s", true)
        embed.addField("Distance where speed is reached", "${round((maxDistance-minDistance)*2)}mm", true)
        embed.addField("Movement length", "${length}mm", true)
        embed.addField("Time to full speed", "${(timeToFullSpeed)}s", true)
        embed.addField("Distance to full speed", "${round((distanceToFullSpeed))}mm", true)

        event.replyEmbeds(embed.build()).addFile(temp).queue()
        temp.delete()
    }
}