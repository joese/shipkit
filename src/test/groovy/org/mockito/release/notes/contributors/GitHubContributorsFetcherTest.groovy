package org.mockito.release.notes.contributors

import org.mockito.release.notes.vcs.DefaultContribution
import org.mockito.release.notes.vcs.GitCommit
import spock.lang.Specification
import spock.lang.Subject

class GitHubContributorsFetcherTest extends Specification {

    @Subject fetcher = new GitHubContributorsFetcher()

    def readOnlyToken = "a0a4c0f41c200f7c653323014d6a72a127764e17"

    def "fetches contributors from GitHub"() {
        def contributions = new LinkedList<DefaultContribution>()
        contributions.add(new DefaultContribution(new GitCommit(
                "1", "continuous.delivery.drone@gmail.com", "Continuous Delivery Drone", "msg 1")))
        contributions.add(new DefaultContribution(new GitCommit(
                "2", "szczepiq@gmail.com", "Szczepan Faber", "msg 2")))

        when:
        def contributors = fetcher.fetchContributors(readOnlyToken, contributions, "", "HEAD")

        then:
        contributors.get("Continuous Delivery Drone").login == "continuous-delivery-drone"
        contributors.get("Continuous Delivery Drone").name == "Continuous Delivery Drone"
        contributors.get("Continuous Delivery Drone").profileUrl == "https://github.com/continuous-delivery-drone"
        contributors.get("Szczepan Faber").login == "szczepiq"
        contributors.get("Szczepan Faber").name == "Szczepan Faber"
        contributors.get("Szczepan Faber").profileUrl == "https://github.com/szczepiq"
    }

    def "dont fetch contributors when empty contributions"() {
        when:
        def contributors = fetcher.fetchContributors(readOnlyToken, Collections.emptyList(), "", "HEAD")

        then:
        contributors.isEmpty()
    }
}
