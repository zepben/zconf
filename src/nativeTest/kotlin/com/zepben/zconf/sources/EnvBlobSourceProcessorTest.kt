/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class EnvBlobSourceProcessorTest : FunSpec({
    test("handles non existent environment variables") {
        EnvBlobSourceProcessor("DEFINITELY_NOT_A_REAL_ENV_VAR").properties
    }

    test("handles non json environment variables") {
        EnvBlobSourceProcessor("FAKE") { _ -> "" }.properties
    }

    //{
    //    "menu": {
    //        "id": "file",
    //        "value": "File",
    //        "popup": {
    //            "menuitem": [
    //                {
    //                    "value": "New",
    //                    "onclick": "CreateNewDoc()"
    //                },
    //                {
    //                    "value": "Open",
    //                    "onclick": "OpenDoc()"
    //                },
    //                {
    //                    "value": "Close",
    //                    "onclick": "CloseDoc()"
    //                }
    //            ]
    //        }
    //    }
    //}

    test("parses a complex JSON var") {
        val envVal = "eyJtZW51IjogewogICJpZCI6ICJmaWxlIiwKICAidmFsdWUiOiAiRmlsZSIsCiAgInBvcHVwIjogewogICAgIm1lbnVpdGVtIjogWwogICAgICB7InZhbHVlIjogIk5ldyIsICJvbmNsaWNrIjogIkNyZWF0ZU5ld0RvYygpIn0sCiAgICAgIHsidmFsdWUiOiAiT3BlbiIsICJvbmNsaWNrIjogIk9wZW5Eb2MoKSJ9LAogICAgICB7InZhbHVlIjogIkNsb3NlIiwgIm9uY2xpY2siOiAiQ2xvc2VEb2MoKSJ9CiAgICBdCiAgfSwKICAiYSI6IHsKICAgICJiIjogWzEsMiwzLDRdLAogICAgImMiOiBbWzFdLCBbMl0sIFszXSwgWzRdXQogIH0KfX0="
        val config = EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        config["menu.id"] shouldBe ConfigValue("file")
        config["menu.value"] shouldBe ConfigValue("File")
        config["menu.popup.menuitem.0.value"] shouldBe ConfigValue("New")
        config["menu.a.b.1"] shouldBe ConfigValue("2")
        config["menu.a.c.1.0"] shouldBe ConfigValue("2")
    }

    test("safely fails for invalid b64 data") {
        val envVal = "eyJtZW51IjogewogICJpZCI6ICJmaWxlIiwKICAidmFsdWUiOiAiRmlsZSIsCiAgInBvcH"
        EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties
    }

    test("preserves json element when parsing") {
        val envVal = "ewogICAgIm1vZGVsT3B0aW9ucyI6IHsKICAgICAgICAic2NlbmFyaW9zIjogWyJiYXNlIl0sCiAgICAgICAgInllYXJzIjogWzIwMjNdLAogICAgICAgICJmZWVkZXJzIjogWyJmZWVkZXIxIl0KICAgIH0sCiAgICAibW9kdWxlc0NvbmZpZ3VyYXRpb24iOiB7CiAgICAgICAgImNvbW1vbiI6IHsKICAgICAgICAgICAgImZpeGVkVGltZSI6IG51bGwsCiAgICAgICAgICAgICJ0aW1lUGVyaW9kIjogewogICAgICAgICAgICAgICAgInN0YXJ0VGltZSI6ICIyMDIyLTA0LTAxVDAwOjAwOjAwWiIsCiAgICAgICAgICAgICAgICAiZW5kVGltZSI6ICIyMDIzLTA0LTAxVDAwOjAwOjAwWiIKICAgICAgICAgICAgfQogICAgICAgIH0sCiAgICAgICAgImdlbmVyYXRvciI6IHsKICAgICAgICAgICAgIm1vZGVsIjogewogICAgICAgICAgICAgICAgInZtUHUiOiAxLjAsCiAgICAgICAgICAgICAgICAidk1pblB1IjogMC44LAogICAgICAgICAgICAgICAgInZNYXhQdSI6IDEuMTUsCiAgICAgICAgICAgICAgICAibG9hZE1vZGVsIjogMSwKICAgICAgICAgICAgICAgICJjb2xsYXBzZVNXRVIiOiBmYWxzZSwKICAgICAgICAgICAgICAgICJjYWxpYnJhdGlvbiI6IGZhbHNlLAogICAgICAgICAgICAgICAgInBGYWN0b3JCYXNlRXhwb3J0cyI6IG51bGwsCiAgICAgICAgICAgICAgICAicEZhY3RvckJhc2VJbXBvcnRzIjogbnVsbCwKICAgICAgICAgICAgICAgICJwRmFjdG9yRm9yZWNhc3RQdiI6IDEuMCwKICAgICAgICAgICAgICAgICJmaXhTaW5nbGVQaGFzZUxvYWRzIjogdHJ1ZSwKICAgICAgICAgICAgICAgICJtYXhTaW5nbGVQaGFzZUxvYWQiOiAzMDAwMC4wLAogICAgICAgICAgICAgICAgImZpeE92ZXJsb2FkaW5nQ29uc3VtZXJzIjogdHJ1ZSwKICAgICAgICAgICAgICAgICJtYXhMb2FkVHhSYXRpbyI6IDMuMCwKICAgICAgICAgICAgICAgICJtYXhHZW5UeFJhdGlvIjogMTAuMCwKICAgICAgICAgICAgICAgICJmaXhVbmRlcnNpemVkU2VydmljZUxpbmVzIjogdHJ1ZSwKICAgICAgICAgICAgICAgICJtYXhMb2FkU2VydmljZUxpbmVSYXRpbyI6IDEuNSwKICAgICAgICAgICAgICAgICJtYXhMb2FkTHZMaW5lUmF0aW8iOiAyLjAsCiAgICAgICAgICAgICAgICAiY29sbGFwc2VMdk5ldHdvcmtzIjogZmFsc2UsCiAgICAgICAgICAgICAgICAiZmVlZGVyU2NlbmFyaW9BbGxvY2F0aW9uU3RyYXRlZ3kiOiAiQURESVRJVkUiLAogICAgICAgICAgICAgICAgImNsb3NlZExvb3BWUmVnRW5hYmxlZCI6IHRydWUsCiAgICAgICAgICAgICAgICAiY2xvc2VkTG9vcFZSZWdSZXBsYWNlQWxsIjogdHJ1ZSwKICAgICAgICAgICAgICAgICJjbG9zZWRMb29wVlJlZ1NldFBvaW50IjogMC45ODUsCiAgICAgICAgICAgICAgICAiY2xvc2VkTG9vcFZCYW5kIjogMi4wLAogICAgICAgICAgICAgICAgImNsb3NlZExvb3BUaW1lRGVsYXkiOiAxMDAsCiAgICAgICAgICAgICAgICAiY2xvc2VkTG9vcFZMaW1pdCI6IDEuMSwKICAgICAgICAgICAgICAgICJkZWZhdWx0VGFwQ2hhbmdlclRpbWVEZWxheSI6IDEwMCwKICAgICAgICAgICAgICAgICJkZWZhdWx0VGFwQ2hhbmdlclNldFBvaW50UHUiOiAxLjAsCiAgICAgICAgICAgICAgICAiZGVmYXVsdFRhcENoYW5nZXJCYW5kIjogMi4wLAogICAgICAgICAgICAgICAgInNwbGl0UGhhc2VEZWZhdWx0TG9hZExvc3NQZXJjZW50YWdlIjogMC40LAogICAgICAgICAgICAgICAgInNwbGl0UGhhc2VMVktWIjogMC4yNSwKICAgICAgICAgICAgICAgICJzd2VyVm9sdGFnZVRvTGluZVZvbHRhZ2UiOiB7CiAgICAgICAgICAgICAgICAgICAgIjIzMCI6IDQwMCwKICAgICAgICAgICAgICAgICAgICAiMjQwIjogNDE1LAogICAgICAgICAgICAgICAgICAgICIyNTAiOiA0MzMsCiAgICAgICAgICAgICAgICAgICAgIjYzNTAiOiAxMTAwMCwKICAgICAgICAgICAgICAgICAgICAiNjQwMCI6IDExMDAwLAogICAgICAgICAgICAgICAgICAgICIxMjcwMCI6IDIyMDAwLAogICAgICAgICAgICAgICAgICAgICIxOTEwMCI6IDMzMDAwCiAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgImxvYWRQbGFjZW1lbnQiOiAiUEVSX1VTQUdFX1BPSU5UIiwKICAgICAgICAgICAgICAgICJsb2FkSW50ZXJ2YWxMZW5ndGhIb3VycyI6IDAuNSwKICAgICAgICAgICAgICAgICJtZXRlclBsYWNlbWVudENvbmZpZyI6IHsKICAgICAgICAgICAgICAgICAgICAiZmVlZGVySGVhZCI6IHRydWUsCiAgICAgICAgICAgICAgICAgICAgImRpc3RUcmFuc2Zvcm1lcnMiOiB0cnVlLAogICAgICAgICAgICAgICAgICAgICJzd2l0Y2hNZXRlclBsYWNlbWVudENvbmZpZ3MiOiBbewogICAgICAgICAgICAgICAgICAgICAgICAgICAgIm1ldGVyU3dpdGNoQ2xhc3MiOiAiUkVDTE9TRVIiLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgIm5hbWVQYXR0ZXJuIjogIi4qIgogICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgICAgICAiZW5lcmd5Q29uc3VtZXJNZXRlckdyb3VwIjogbnVsbAogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJzZWVkIjogbnVsbCwKICAgICAgICAgICAgICAgICJkZWZhdWx0TG9hZFdhdHRzIjogbnVsbCwKICAgICAgICAgICAgICAgICJkZWZhdWx0R2VuV2F0dHMiOiBudWxsLAogICAgICAgICAgICAgICAgImRlZmF1bHRMb2FkVmFyIjogbnVsbCwKICAgICAgICAgICAgICAgICJkZWZhdWx0R2VuVmFyIjogbnVsbCwKICAgICAgICAgICAgICAgICJ0cmFuc2Zvcm1lclRhcFNldHRpbmdzIjogbnVsbAogICAgICAgICAgICB9LAogICAgICAgICAgICAic29sdmUiOiB7CiAgICAgICAgICAgICAgICAibm9ybVZNaW5QdSI6IDAuOSwKICAgICAgICAgICAgICAgICJub3JtVk1heFB1IjogMS4wNTQsCiAgICAgICAgICAgICAgICAiZW1lcmdWTWluUHUiOiAwLjgsCiAgICAgICAgICAgICAgICAiZW1lcmdWTWF4UHUiOiAxLjEsCiAgICAgICAgICAgICAgICAiYmFzZUZyZXF1ZW5jeSI6IDUwLAogICAgICAgICAgICAgICAgInZvbHRhZ2VCYXNlcyI6IFswLjQsIDAuNDMzLCA2LjYsIDExLjAsIDIyLjAsIDMzLjAsIDY2LjAsIDEzMi4wXSwKICAgICAgICAgICAgICAgICJtYXhJdGVyIjogMjUsCiAgICAgICAgICAgICAgICAibWF4Q29udHJvbEl0ZXIiOiAyMCwKICAgICAgICAgICAgICAgICJtb2RlIjogIllFQVJMWSIsCiAgICAgICAgICAgICAgICAic3RlcFNpemVNaW51dGVzIjogNjAKICAgICAgICAgICAgfSwKICAgICAgICAgICAgInJhd1Jlc3VsdHMiOiB7CiAgICAgICAgICAgICAgICAiZW5lcmd5TWV0ZXJWb2x0YWdlc1JhdyI6IHRydWUsCiAgICAgICAgICAgICAgICAiZW5lcmd5TWV0ZXJzUmF3IjogdHJ1ZSwKICAgICAgICAgICAgICAgICJyZXN1bHRzUGVyTWV0ZXIiOiB0cnVlLAogICAgICAgICAgICAgICAgIm92ZXJsb2Fkc1JhdyI6IHRydWUsCiAgICAgICAgICAgICAgICAidm9sdGFnZUV4Y2VwdGlvbnNSYXciOiB0cnVlCiAgICAgICAgICAgIH0KICAgICAgICB9CiAgICB9LAogICAgIm1vZGVsU3RvcmUiOiB7CiAgICAgICAgImJsb2JQYXRoIjogIm1vZGVscy8xL1RFU1QgT1BFTkRTUyBNT0RFTCAxLnppcCIKICAgIH0KfQo="
        val config = EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        Json.encodeToString(config.toJson()) shouldBe """{"modelOptions":{"scenarios":["base"],"years":["2023"],"feeders":["feeder1"]},"modulesConfiguration":{"common":{"timePeriod":{"startTime":"2022-04-01T00:00:00Z","endTime":"2023-04-01T00:00:00Z"}},"generator":{"model":{"vmPu":"1.0","vMinPu":"0.8","vMaxPu":"1.15","loadModel":"1","collapseSWER":"false","calibration":"false","pFactorForecastPv":"1.0","fixSinglePhaseLoads":"true","maxSinglePhaseLoad":"30000.0","fixOverloadingConsumers":"true","maxLoadTxRatio":"3.0","maxGenTxRatio":"10.0","fixUndersizedServiceLines":"true","maxLoadServiceLineRatio":"1.5","maxLoadLvLineRatio":"2.0","collapseLvNetworks":"false","feederScenarioAllocationStrategy":"ADDITIVE","closedLoopVRegEnabled":"true","closedLoopVRegReplaceAll":"true","closedLoopVRegSetPoint":"0.985","closedLoopVBand":"2.0","closedLoopTimeDelay":"100","closedLoopVLimit":"1.1","defaultTapChangerTimeDelay":"100","defaultTapChangerSetPointPu":"1.0","defaultTapChangerBand":"2.0","splitPhaseDefaultLoadLossPercentage":"0.4","splitPhaseLVKV":"0.25","swerVoltageToLineVoltage":{"230":"400","240":"415","250":"433","6350":"11000","6400":"11000","12700":"22000","19100":"33000"},"loadPlacement":"PER_USAGE_POINT","loadIntervalLengthHours":"0.5","meterPlacementConfig":{"feederHead":"true","distTransformers":"true","switchMeterPlacementConfigs":[{"meterSwitchClass":"RECLOSER","namePattern":".*"}]}},"solve":{"normVMinPu":"0.9","normVMaxPu":"1.054","emergVMinPu":"0.8","emergVMaxPu":"1.1","baseFrequency":"50","voltageBases":["0.4","0.433","6.6","11.0","22.0","33.0","66.0","132.0"],"maxIter":"25","maxControlIter":"20","mode":"YEARLY","stepSizeMinutes":"60"},"rawResults":{"energyMeterVoltagesRaw":"true","energyMetersRaw":"true","resultsPerMeter":"true","overloadsRaw":"true","voltageExceptionsRaw":"true"}}},"modelStore":{"blobPath":"models/1/TEST OPENDSS MODEL 1.zip"}}"""

    }
})
