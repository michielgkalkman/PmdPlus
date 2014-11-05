PmdPlus
=======

**Warning**

	This is a work in progress.	


This project is my attempt at building some PMD rules. The most important one is directed at reducting the number of expressions used, which results in better readable code and possible performance improvements.

Example:


            if (child.getNumberOfDecks() > maxChildDecks)
            {
                maxChildDecks = child.getNumberOfDecks();
            }

Could be reduced to:


            int numberOfDecks = child.getNumberOfDecks();
			if (numberOfDecks > maxChildDecks)
            {
                maxChildDecks = numberOfDecks;
            }

Resulting in one less call to getNumberOfDecks().



# Usage #

## Maven ##


Add:


	...
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-pmd-plugin</artifactId>
					<version>3.2</version>
					<configuration>
						<rulesets>
							<ruleset>/rulesets/java/dup.xml</ruleset>
						</rulesets>
					</configuration>
					<dependencies>
						<dependency>
							<groupId>org.taHjaj.wo</groupId>
							<artifactId>PmdPlus</artifactId>
							<version>0.0.1-SNAPSHOT</version>
						</dependency>
					</dependencies>
				</plugin>
			</plugins>
		</pluginManagement>
	</build>


And:

    	<reporting>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-pmd-plugin</artifactId>
			</plugin>
		</plugins>
	</reporting>

Invoke with:

    mvn pmd:pmd


# There will be more #

.. if time allows it.