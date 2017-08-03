## Working with ICFPC VM

- Download latest VM from http://events.inf.ed.ac.uk/icfpcontest2017/vm/. 
- `vboxmanage import icfp2017-contest-2017_08_03.ova` (use specific VM file name if changed)
- `vboxmanage startvm "icfp2017-contest_1" --type headless`
- `vboxmanage showvminfo icfp2017-contest_1 | grep Rule`
- look for port number
- `ssh -p 2222 punter@localhost` (use port from previous step)
- use password `icfp2017`