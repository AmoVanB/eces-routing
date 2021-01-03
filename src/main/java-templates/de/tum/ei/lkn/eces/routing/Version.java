package de.tum.ei.lkn.eces.routing;

/**
 * Version file automatically parsed by maven to contain the version, group
 * id and artifact id of the project.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public final class Version {
	public static final String VERSION = "${project.version}";
	public static final String GROUPID = "${project.groupId}";
	public static final String ARTIFACTID = "${project.artifactId}";
	public static final String FQID = GROUPID + "" + ARTIFACTID;
}
