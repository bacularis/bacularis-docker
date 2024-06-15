/**
 * Build configuration variables.
 */
PROJECT_URL = 'https://github.com/bacularis'
EXTERNAL_URL = "https://bacularis.app/downloads/bacularis-external-${params.BACULARIS_VERSION}.tar.gz"
REGISTRY_URL = 'http://10.0.0.1:5000'
NAME = 'bacularis'
PACKAGES = ['standalone', 'web', 'api-dir', 'api-sd', 'api-fd']
ALPINE_VERSION='3.20'
DEBIAN_VERSION='trixie-slim'
DEBIAN_CODENAME='trixie'

def pullRepos() {
	dir ("${NAME}-app") {
		checkout([$class: 'GitSCM',
				branches: [[name: "${params.BACULARIS_VERSION}"]],
				doGenerateSubmoduleConfigurations: false,
				extensions: [],
				gitTool: 'Default',
				submoduleCfg: [],
				userRemoteConfigs: [[url: "${PROJECT_URL}/${NAME}-app"]]
		])
	}
	dir("${NAME}-common") {
		checkout([$class: 'GitSCM',
				branches: [[name: "${params.BACULARIS_VERSION}"]],
				doGenerateSubmoduleConfigurations: false,
				extensions: [],
				gitTool: 'Default',
				submoduleCfg: [],
				userRemoteConfigs: [[url: "${PROJECT_URL}/${NAME}-common"]]
		])
	}
	dir ("${NAME}-api") {
		checkout([$class: 'GitSCM',
				branches: [[name: "${params.BACULARIS_VERSION}"]],
				doGenerateSubmoduleConfigurations: false,
				extensions: [],
				gitTool: 'Default',
				submoduleCfg: [],
				userRemoteConfigs: [[url: "${PROJECT_URL}/${NAME}-api"]]
		])
	}
	dir ("${NAME}-web") {
		checkout([$class: 'GitSCM',
				branches: [[name: "${params.BACULARIS_VERSION}"]],
				doGenerateSubmoduleConfigurations: false,
				extensions: [],
				gitTool: 'Default',
				submoduleCfg: [],
				userRemoteConfigs: [[url: "${PROJECT_URL}/${NAME}-web"]]
		])
	}
	dir ("${NAME}-external") {
		sh """
			if [ ! -e 'bacularis-external-${params.BACULARIS_VERSION}.tar.gz' -o ! -d 'vendor' ]
			then
				wget "$EXTERNAL_URL"
				tar --strip-components=1 -zxvf bacularis-external-${params.BACULARIS_VERSION}.tar.gz
			fi
		"""
	}
}

def prepareProject() {
	sh """
		mkdir -p \\
			\"${NAME}/protected/vendor/${NAME}/${NAME}-common\" \\
			\"${NAME}/protected/vendor/${NAME}/${NAME}-api\" \\
			\"${NAME}/protected/vendor/${NAME}/${NAME}-web\" \\
			\"${NAME}/protected/runtime\" \\
			\"${NAME}/htdocs/assets\"
		cp -a \"${NAME}-common/project/\"* \"${NAME}/\"
		cp -r \"${NAME}-common/\"* \"${NAME}/protected/vendor/${NAME}/${NAME}-common/\"
		cp -r \"${NAME}-api/\"* \"${NAME}/protected/vendor/${NAME}/${NAME}-api/\"
		cp -r \"${NAME}-web/\"* \"${NAME}/protected/vendor/${NAME}/${NAME}-web/\"
		cp -r \"${NAME}-external/vendor/\"* \"${NAME}/protected/vendor/\"
		cp \"${NAME}/protected/samples/webserver/${NAME}.users.sample\" \"${NAME}/protected/vendor/${NAME}/${NAME}-api/API/Config/${NAME}.users\"
		cp \"${NAME}/protected/samples/webserver/${NAME}.users.sample\" \"${NAME}/protected/vendor/${NAME}/${NAME}-web/Web/Config/${NAME}.users\"
		ln -sf \"vendor/${NAME}/${NAME}-common/Common\" \"${NAME}/protected/\"
		ln -sf \"vendor/${NAME}/${NAME}-api/API\" \"${NAME}/protected/\"
		ln -sf \"vendor/${NAME}/${NAME}-web/Web\" \"${NAME}/protected/\"
		cp \"${NAME}/protected/vendor/bower-asset/fontawesome/css/all.min.css\" \"${NAME}/htdocs/themes/Baculum-v2/fonts/css/fontawesome-all.min.css\"
		cp -r \"${NAME}/protected/vendor/bower-asset/fontawesome/webfonts/\"* \"${NAME}/htdocs/themes/Baculum-v2/fonts/webfonts/\"
	"""
}

def prepareDockerfile(String package_name, String os_variant) {
	sh """
		cp 'Dockerfile-""" + os_variant + """.template' 'Dockerfile'
		sed -i \\
			-e "s!%%BACULARIS_VERSION%%!${params.BACULARIS_VERSION}!" \\
			-e "s!%%ALPINE_VERSION%%!${ALPINE_VERSION}!" \\
			-e "s!%%DEBIAN_VERSION%%!${DEBIAN_VERSION}!" \\
			-e "s!%%PACKAGE_NAME%%!""" + package_name + """!" \\
			'Dockerfile'
	"""
}

def getImageName(String package_name, String package_version, String os_variant) {
	if (os_variant == 'debian') {
		os_variant = "${DEBIAN_CODENAME}"
	}
	def image_name = package_name + ':' + package_version + '-' + os_variant
	return image_name
}

def getPackageName(String pname) {
	def package_name = NAME + '-' + pname
	return package_name
}

def buildDockerImages(String package_version) {
	def package_name
	def osv = "${params.OS_VARIANTS}".split(',')
	for (int i = 0; i < osv.size(); i++) {
		for (int j = 0; j < PACKAGES.size(); j++) {
			prepareDockerfile(PACKAGES[j], osv[i])
			package_name = getPackageName(PACKAGES[j])
			buildDockerImage(package_name, package_version, osv[i])
		}
	}
}

def buildDockerImage(String package_name, String package_version, String os_variant) {
	docker.withRegistry("${REGISTRY_URL}") {
		def image_name = getImageName(package_name, package_version, os_variant)
		def customImage = docker.build(image_name)
		customImage.push()
	}
}

return this
