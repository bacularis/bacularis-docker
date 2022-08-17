def gv
pipeline {
	agent any
	parameters {
		string(name: 'BACULARIS_VERSION', defaultValue: 'master', description: 'Bacularis version to build.')
		string(name: 'OS_VARIANTS', defaultValue: 'alpine,debian', description: 'Bacularis OS variants to build.')
	}
	stages {
		stage('Init') {
			steps {
				script {
					gv = load 'build.groovy'
					gv.pullRepos()
				}
			}
		 }
		stage('Preparation') {
			steps {
				script {
					gv.prepareProject()
				}
			}
		}
		stage('Build') {
			steps {
				script {
					gv.buildDockerImages("${params.BACULARIS_VERSION}")
				}
			}
		}
	}
}

