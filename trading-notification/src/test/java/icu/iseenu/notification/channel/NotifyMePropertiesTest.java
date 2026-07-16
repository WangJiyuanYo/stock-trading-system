package icu.iseenu.notification.channel;

import icu.iseenu.infra.config.NotificationProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyMePropertiesTest {

    @Test
    void shouldMergeMultipleAndLegacyUuidsWithoutDuplicates() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUuids(List.of("uuid-1", "uuid-2, uuid-3", " "));
        properties.setUuid("uuid-3,uuid-4");

        assertThat(properties.getResolvedUuids())
                .containsExactly("uuid-1", "uuid-2", "uuid-3", "uuid-4");
    }

    @Test
    void shouldIgnoreEmptyUuidConfiguration() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUuids(List.of("", "  "));
        properties.setUuid(null);

        assertThat(properties.getResolvedUuids()).isEmpty();
    }

    @Test
    void shouldResolveSubscribedUsersByScene() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUsers(List.of(
                user("A", "uuid-a", "stock", "roco"),
                user("B", "uuid-b", "roco"),
                user("C", "uuid-c", "stock")
        ));

        assertThat(properties.getResolvedUuids("stock"))
                .containsExactlyInAnyOrder("uuid-a", "uuid-c");
        assertThat(properties.getResolvedUuids("roco"))
                .containsExactlyInAnyOrder("uuid-a", "uuid-b");
    }

    @Test
    void shouldSupportWildcardSubscription() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUsers(List.of(
                user("admin", "admin-uuid", "*"),
                user("stock-user", "stock-uuid", "stock")
        ));

        assertThat(properties.getResolvedUuids("roco"))
                .containsExactly("admin-uuid");
    }

    @Test
    void shouldNotFallBackWhenUsersConfiguredButNoOneSubscribes() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUuids(List.of("legacy-uuid"));
        properties.setUsers(List.of(user("A", "uuid-a", "roco")));

        assertThat(properties.getResolvedUuids("stock")).isEmpty();
    }

    @Test
    void shouldIgnoreUsersWithoutUuid() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUsers(List.of(user("A", "", "stock")));

        assertThat(properties.getResolvedUuids("stock")).isEmpty();
    }

    @Test
    void shouldParseDynamicUsersConfig() {
        NotificationProperties.NotifyMe properties = new NotificationProperties.NotifyMe();
        properties.setUsersConfig(
                "A|uuid-a|stock,roco; B|uuid-b|roco; D|uuid-d|stock,roco");

        assertThat(properties.getResolvedUuids("stock"))
                .containsExactly("uuid-a", "uuid-d");
        assertThat(properties.getResolvedUuids("roco"))
                .containsExactly("uuid-a", "uuid-b", "uuid-d");
    }

    private NotificationProperties.NotifyMe.NotifyMeUser user(
            String name, String uuid, String... scenes) {
        NotificationProperties.NotifyMe.NotifyMeUser user =
                new NotificationProperties.NotifyMe.NotifyMeUser();
        user.setName(name);
        user.setUuid(uuid);
        user.setSubscribe(List.of(scenes));
        return user;
    }
}
