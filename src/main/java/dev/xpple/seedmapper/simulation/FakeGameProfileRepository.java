package dev.xpple.seedmapper.simulation;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;

public class FakeGameProfileRepository implements GameProfileRepository {
    @Override
    public void findProfilesByNames(String[] names, Agent agent, ProfileLookupCallback callback) {
    }
}
