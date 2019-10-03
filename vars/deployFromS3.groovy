#!/usr/bin/env groovy

def call(Map args = [:]) {
def artifactFileName='trading-diginex-v'+args.releaseVersion+'.tar.gz'
println("downloading $artifactFileName from S3..")
sh (
    label: "Deploy from S3",
    script: """
      #!/bin/bash -eu
      aws --version
      aws s3 cp s3://trading-deployment-s3-main/${artifactFileName} ${artifactFileName}
      ls -lah
    """
  )
}