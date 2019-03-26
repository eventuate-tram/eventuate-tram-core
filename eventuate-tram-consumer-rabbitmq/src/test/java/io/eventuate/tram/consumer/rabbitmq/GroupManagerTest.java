package io.eventuate.tram.consumer.rabbitmq;

import com.google.common.collect.ImmutableSet;
import io.eventuate.util.test.async.Eventually;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.Consumer;

@RunWith(SpringRunner.class)
public class GroupManagerTest {

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  private String uniqueId = UUID.randomUUID().toString();

  @Test
  public void testMemberAdded() {
    Set<String> groupMemberIds = new HashSet<>();

    createGroupManager(createCuratorFramework(), members -> {
      groupMemberIds.clear();
      groupMemberIds.addAll(members);
    });

    createGroupMember(createCuratorFramework(), "1");

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of("1"), groupMemberIds));
  }

  @Test
  public void testMemberRemovedWhenClosed() {
    ZkGroupMember groupMember = createGroupMember(createCuratorFramework(), "1");

    Set<String> groupMemberIds = new HashSet<>();

    createGroupManager(createCuratorFramework(), members -> {
      groupMemberIds.clear();
      groupMemberIds.addAll(members);
    });

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of("1"), groupMemberIds));

    groupMember.remove();

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of(), groupMemberIds));
  }

  @Test
  public void testMemberRemovedWhenCuratorClosed() throws Exception {
    CuratorFramework curatorFramework = createCuratorFramework();

    createGroupMember(curatorFramework, "1");

    Set<String> groupMemberIds = new HashSet<>();

    createGroupManager(createCuratorFramework(), members -> {
      groupMemberIds.clear();
      groupMemberIds.addAll(members);
    });

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of("1"), groupMemberIds));

    curatorFramework.close();

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of(), groupMemberIds));
  }

  @Test
  public void testAddRemoveSeveralMembers() throws Exception {
    Set<String> groupMemberIds = new HashSet<>();

    createGroupManager(createCuratorFramework(), members -> {
      groupMemberIds.clear();
      groupMemberIds.addAll(members);
    });

    LinkedList<ZkGroupMember> groupMembers = new LinkedList<>();

    for (int i = 0; i < 5; i++) {
      groupMembers.add(createGroupMember(createCuratorFramework(), String.valueOf(i)));
    }


    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of("0", "1", "2", "3", "4"), groupMemberIds));

    groupMembers.poll().remove();
    groupMembers.poll().remove();

    Eventually.eventually(() -> Assert.assertEquals(ImmutableSet.of("2", "3", "4"), groupMemberIds));
  }

  private ZkMemberGroupManager createGroupManager(CuratorFramework curatorFramework, Consumer<Set<String>> groupMembersUpdatedCallback) {
    return new ZkMemberGroupManager(curatorFramework, uniqueId, groupMembersUpdatedCallback);
  }

  private ZkGroupMember createGroupMember(CuratorFramework curatorFramework, String id) {
    return new ZkGroupMember(curatorFramework, uniqueId, id);
  }

  private CuratorFramework createCuratorFramework() {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkUrl,
            new ExponentialBackoffRetry(1000, 5));

    curatorFramework.start();

    return curatorFramework;
  }
}
