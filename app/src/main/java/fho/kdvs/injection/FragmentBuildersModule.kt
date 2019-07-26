/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fho.kdvs.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fho.kdvs.broadcast.BroadcastDetailsFragment
import fho.kdvs.home.HomeFragment
import fho.kdvs.home.StaffDetailsFragment
import fho.kdvs.player.PlayerFragment
import fho.kdvs.schedule.ScheduleFragment
import fho.kdvs.schedule.ScheduleSelectionFragment
import fho.kdvs.schedule.ShowSearchFragment
import fho.kdvs.settings.SettingsFragment
import fho.kdvs.show.ShowDetailsFragment
import fho.kdvs.topmusic.TopMusicDetailsFragment
import fho.kdvs.track.TrackDetailsFragment

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributePlayerFragment(): PlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeStaffDetailsFragment(): StaffDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeScheduleFragment(): ScheduleFragment

    @ContributesAndroidInjector
    abstract fun contributeScheduleSelectionFragment(): ScheduleSelectionFragment

    @ContributesAndroidInjector
    abstract fun contributeShowSearchFragment(): ShowSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeShowDetailsFragment(): ShowDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeBroadcastDetailsFragment(): BroadcastDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeTrackDetailsFragment(): TrackDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeTopMusicDetailsFragment(): TopMusicDetailsFragment
}
